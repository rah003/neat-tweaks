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
package com.neatresults;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Consumer that splits treating of incoming data based on result of predicate evaluation.
 * 
 * @param <T>
 *            Type of consumed data.
 */
public class PredicateSplitterConsumer<T> implements Consumer<T>
{
    private Predicate<T> predicate;
    private Consumer<T> positive;
    private Consumer<T> negative;
    private Consumer<T> both;

    public PredicateSplitterConsumer(Predicate<T> predicate, Consumer<T> positive, Consumer<T> negative) {
        this(predicate, positive, negative, null);
    }

    public PredicateSplitterConsumer(Predicate<T> predicate, Consumer<T> positive, Consumer<T> negative, Consumer<T> both)
    {
        this.predicate = predicate;
        this.positive = positive;
        this.negative = negative;
        this.both = both;
    }

    @Override
    public void accept(T t)
    {
        if (predicate.test(t)) {
            positive.accept(t);
        } else {
            negative.accept(t);
        }
        if (both != null) {
            both.accept(t);
        }
    }
}