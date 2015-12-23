package com.neatresults;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

/**
 * Some utils for making life w/ java 8 easier.
 */
public class Java8Util {

    private Java8Util() {
    }

    /**
     * Turns an iterator into stream.
     */
    public static <T> Stream<T> asStream(Iterator<T> iterator) {
        int characteristics = Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, characteristics);

        boolean parallel = false;
        Stream<T> stream = StreamSupport.stream(spliterator, parallel);
        return stream;
    }

    public static Stream<Property> asPropertyStream(PropertyIterator properties) {
        return asStream(properties);
    }

    public static Stream<Node> asNodeStream(NodeIterator nodes) {
        return asStream(nodes);
    }

    public static String getName(Property p) {
        try {
            return p.getName();
        } catch (RepositoryException e) {
            return null;
        }
    }

    public static String getName(Node n) {
        try {
            return n.getName();
        } catch (RepositoryException e) {
            return null;
        }
    }
}
