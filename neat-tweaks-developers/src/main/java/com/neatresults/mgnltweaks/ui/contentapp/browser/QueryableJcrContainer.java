/**
 *
 * Copyright 2015 by Jan Haderka <jan.haderka@neatresults.com>
 *
 * This file is part of neat-tweaks module.
 *
 * Neat-tweaks is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * Neat-tweaks is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with neat-tweaks.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0 <http://www.gnu.org/licenses/gpl.txt>
 *
 * Should you require distribution under alternative license in order to
 * use neat-tweaks commercially, please contact owner at the address above.
 *
 */
package com.neatresults.mgnltweaks.ui.contentapp.browser;

import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinition;
import info.magnolia.ui.workbench.search.SearchJcrContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HierarchicalJcrContainer that can change it's root at runtime.
 */
public class QueryableJcrContainer extends SearchJcrContainer {

    private static final Logger log = LoggerFactory.getLogger(QueryableJcrContainer.class);
    private String path;
    private String workspace;

    public QueryableJcrContainer(JcrContentConnectorDefinition definition, final String path) {
        super(definition);
        this.path = path;
    }

    @Override
    protected QueryResult executeQuery(String statement, String language, long limit, long offset) throws RepositoryException {
        log.debug("ST: {}", statement);
        String[] segments = StringUtils.split(path, "/");
        // templates needs 2 queries - one for website and one for config
        if ("templates".equals(segments[2])) {
            // pages
            QueryResult res1 = super.executeQuery(statement, language, limit, offset);
            // config
            this.setWorkspace(RepositoryConstants.CONFIG);
            statement = StringUtils.substringBefore(statement, " where ") + " where"
                    // id references (availability, autogeneration, ...)
                    + " contains(t.*,'" + segments[1] + ":" + StringUtils.substringAfter(path, "/templates/") + "')"
                    // extends
                    + buildExtends(path);
            log.debug("ST2: {}", statement);
            QueryResult res2 = super.executeQuery(statement, language, limit, offset);
            return new AggregatedQueryResult(res1, res2);
        }
        this.setWorkspace(RepositoryConstants.CONFIG);
        return super.executeQuery(statement, language, limit, offset);
    }

    @Override
    protected String getQueryWhereClause() {
        String[] segments = StringUtils.split(path, "/");
        if ("dialogs".equals(segments[2])) {
            String id = segments[1] + ":" + StringUtils.substringAfter(path, "/dialogs/");
            return " where contains(t.dialog, '" + id + "') or contains(t.dialogName, '" + id + "')" + buildExtends(path);
        } else if ("templates".equals(segments[2])) {
            String id = segments[1] + ":" + StringUtils.substringAfter(path, "/templates/");
            this.setWorkspace(RepositoryConstants.WEBSITE);
            return " where contains(t.[mgnl:template], '" + id + "')";
        } else {
            return " where " + buildExtends(path).substring(3);
        }
    }

    private String buildExtends(String path) {
        // one nasty bugger ... generate all possible combinations of extends for search
        // /a/b/c/d:
        // /d : /a/b/c
        // /c/d : /a/b
        // /b/c/d : /a
        // /a/b/c/d : --
        String end = StringUtils.substringAfterLast(path, "/");
        String limit = StringUtils.substringBeforeLast(path, "/" + end);
        // extends w/ absolute path or too deeply nested
        StringBuilder or = new StringBuilder(" or t.extends like '%" + path + "' ");
        // and all other possible relative extends
        while (StringUtils.isNotEmpty(limit)) {
            or.append(" or (t.extends like '%/" + end + "' and ISDESCENDANTNODE([" + limit + "]))");
            end = StringUtils.substringAfterLast(limit, "/") + "/" + end;
            limit = StringUtils.substringBeforeLast(limit, "/");
        }
        return or.toString();
    }



    private void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    @Override
    public String getWorkspace() {
        return this.workspace;
    }
    public String getReferencePath() {
        return path;
    }

    public void setReferencePath(final String pagePath) {
        this.path = pagePath;
        refresh();
    }

    /**
     * QueryResult capable of sequentially aggregating multiple different result sets.
     */
    public static class AggregatedQueryResult implements QueryResult {

        private QueryResult[] results;

        public AggregatedQueryResult(QueryResult... results) {
            this.results = results;
        }

        @Override
        public String[] getColumnNames() throws RepositoryException {
            HashSet<String> names = new HashSet<String>();
            for (QueryResult result : results) {
                names.addAll(Arrays.asList(result.getColumnNames()));
            }
            return names.toArray(new String[names.size()]);
        }

        @Override
        public RowIterator getRows() throws RepositoryException {
            List<RowIterator> names = new ArrayList<RowIterator>();
            for (QueryResult result : results) {
                names.add(result.getRows());
            }
            return new AggregateRowIterator(names);
        }

        @Override
        public NodeIterator getNodes() throws RepositoryException {
            List<NodeIterator> names = new ArrayList<NodeIterator>();
            for (QueryResult result : results) {
                names.add(result.getNodes());
            }
            return new AggregateNodeIterator(names);
        }

        @Override
        public String[] getSelectorNames() throws RepositoryException {
            HashSet<String> names = new HashSet<String>();
            for (QueryResult result : results) {
                names.addAll(Arrays.asList(result.getSelectorNames()));
            }
            return names.toArray(new String[names.size()]);
        }

    }

    /**
     * RowIterator sequentially iterating over several other iterators.
     */
    public static class AggregateRowIterator implements RowIterator {

        private List<RowIterator> rowIterators;
        private int currentIter = 0;

        public AggregateRowIterator(List<RowIterator> rowIterators) {
            this.rowIterators = rowIterators;
        }

        @Override
        public void skip(long skipNum) {
            long reminder = rowIterators.get(currentIter).getSize() - rowIterators.get(currentIter).getPosition();
            if (reminder > skipNum) {
                rowIterators.get(currentIter).skip(skipNum);
                return;
            } else if (reminder == skipNum) {
                currentIter++;
                return;
            } else {
                skipNum = -reminder;
                currentIter++;
                skip(skipNum);
            }
        }

        @Override
        public long getSize() {
            int size = 0;
            for (RowIterator iter : rowIterators) {
                size += iter.getSize();
            }
            return size;
        }

        @Override
        public long getPosition() {
            return rowIterators.get(currentIter).getPosition();
        }

        @Override
        public boolean hasNext() {
            boolean next = rowIterators.get(currentIter).hasNext();
            while (!next && rowIterators.size() - 1 > currentIter) {
                currentIter++;
                next = rowIterators.get(currentIter).hasNext();
            }
            return next;
        }

        @Override
        public Object next() {
            while (!rowIterators.get(currentIter).hasNext() && rowIterators.size() - 1 > currentIter) {
                currentIter++;
            }
            return rowIterators.get(currentIter).next();
        }

        @Override
        public Row nextRow() {
            while (!rowIterators.get(currentIter).hasNext() && rowIterators.size() - 1 > currentIter) {
                currentIter++;
            }
            return rowIterators.get(currentIter).nextRow();
        }

    }

    /**
     * NodeIterator sequentially iterating over several other iterators.
     */
    public static class AggregateNodeIterator implements NodeIterator {

        private List<NodeIterator> nodeIterators;
        private int currentIter = 0;

        public AggregateNodeIterator(List<NodeIterator> nodeIterators) {
            this.nodeIterators = nodeIterators;
        }

        @Override
        public void skip(long skipNum) {
            long reminder = nodeIterators.get(currentIter).getSize() - nodeIterators.get(currentIter).getPosition();
            if (reminder > skipNum) {
                nodeIterators.get(currentIter).skip(skipNum);
                return;
            } else if (reminder == skipNum) {
                currentIter++;
                return;
            } else {
                skipNum = -reminder;
                currentIter++;
                skip(skipNum);
            }
        }

        @Override
        public long getSize() {
            int size = 0;
            for (NodeIterator iter : nodeIterators) {
                size += iter.getSize();
            }
            return size;
        }

        @Override
        public long getPosition() {
            return nodeIterators.get(currentIter).getPosition();
        }

        @Override
        public boolean hasNext() {
            boolean next = nodeIterators.get(currentIter).hasNext();
            while (!next && nodeIterators.size() - 1 > currentIter) {
                currentIter++;
                next = nodeIterators.get(currentIter).hasNext();
            }
            return next;
        }

        @Override
        public Object next() {
            while (!nodeIterators.get(currentIter).hasNext() && nodeIterators.size() - 1 > currentIter) {
                currentIter++;
            }
            return nodeIterators.get(currentIter).next();
        }

        @Override
        public Node nextNode() {
            while (!nodeIterators.get(currentIter).hasNext() && nodeIterators.size() - 1 > currentIter) {
                currentIter++;
            }
            return nodeIterators.get(currentIter).nextNode();
        }
    }
}
