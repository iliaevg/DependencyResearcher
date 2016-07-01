/*
 * Copyright 2016 iliaevg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.iliaevg.dependencyresearcher;

import com.github.iliaevg.dependencyresearcher.exception.DependencyResearcherException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author iliaevg
 */
public class DependencyChainImpl implements DependencyChain {

    //<editor-fold defaultstate="collapsed" desc="private fields">
    private final List<Entity> value;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    public DependencyChainImpl() {
        this.value = new ArrayList<>();
    }

    public DependencyChainImpl(Collection<Entity> c) {
        this.value = new ArrayList<>(c);
    }

    public DependencyChainImpl(DependencyChain toCopy) {
        value = new ArrayList<>(toCopy.getListValue());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="public static methods">
    /**
     * Определяет эквивалентны ли две цикличные цепочки зависимостей. Например
     * цепочки 1 2 1 и 2 1 2 циклически эквивалентны.
     *
     */
    public static boolean equalsAsCycles(DependencyChain cycleA, DependencyChain cycleB) {

        if (cycleA.size() != cycleB.size()
                || cycleA.isCycled() == false
                || cycleB.isCycled() == false) {
            return false;
        }

        List<Entity> pureCycleA = cycleA.getListValue().subList(0, cycleA.size() - 1);
        List<Entity> pureCycleB = cycleB.getListValue().subList(0, cycleB.size() - 1);

        for (int i = 0; i < pureCycleB.size(); i++) {
            List<Entity> comparableCycleB = new ArrayList<>();
            comparableCycleB.addAll(pureCycleB.subList(i, pureCycleB.size()));
            comparableCycleB.addAll(pureCycleB.subList(0, i));

            if (pureCycleA.equals(comparableCycleB)) {
                return true;
            }
        }

        return false;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="public methods">
    /**
     * Возвращает длину цепочки зависимостей
     */
    @Override
    public int size() {
        return value.size();
    }

    /**
     * Добавляет к цепочке зависимостей следующее звено
     *
     * @throws
     * com.github.iliaevg.dependencyresearcher.exception.DependencyResearcherException
     */
    @Override
    public boolean add(Entity e) throws DependencyResearcherException {
        if (value.isEmpty() == false
                && this.getDependenciesList().contains(e) == false) {
            throw new DependencyResearcherException("Entity " + e + " can't follow chain" + this.toString());
        }
        return value.add(e);

    }

    /**
     * добавляет к текущей цепочке заданную цепочку
     */
    @Override
    public void addDependencyChain(DependencyChainImpl dc) throws DependencyResearcherException {
        if (value.isEmpty() == false
                && this.getDependenciesList().contains(dc.value.get(0)) == false) {
            throw new DependencyResearcherException(
                    "Dependency chain " + dc.value.toString()
                    + " whith starts with " + dc.value.get(0).toString()
                    + " can't follow chain" + this.toString()
            );
        }
        value.addAll(dc.value);
    }

    /**
     * Возвращает список сущностей, зависящих от последнего элемента цепочкис
     */
    @Override
    public List<Entity> getDependenciesList() {
        if (value.size() == 0) {
            return new ArrayList<>();
        } else {
            return value.get(value.size() - 1).getDependenciesList();
        }
    }

    /**
     * Определяет, зациклена ли цепочка
     */
    @Override
    public boolean isCycled() {
        return value.get(0) == value.get(value.size() - 1);
    }

    /**
     * Определяет, есть ли в цепочке подциклы
     */
    @Override
    public boolean containSubCycles() {

        //Проверяем все подпоследовательноти всех длин начиная от длины 2
        for (int sequenceLenght = 2; sequenceLenght < value.size(); sequenceLenght++) {

            for (int baseSequenceIndex = 0; baseSequenceIndex < value.size() - sequenceLenght * 2; baseSequenceIndex++) {

                List<Entity> baseSequence = value.subList(baseSequenceIndex, baseSequenceIndex + sequenceLenght);

                int matchedSequenceIndex = baseSequenceIndex + sequenceLenght + 1;
                List<Entity> matchedSequence = value
                        .subList(matchedSequenceIndex, matchedSequenceIndex + sequenceLenght);

                if (matchedSequence.equals(baseSequence)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String formattedView() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Entity entity : value) {
            stringBuilder.append(entity);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    @Override
    public List<Entity> getListValue() {
        return value;
    }

    @Override
    public Iterator<Entity> iterator() {
        return value.iterator();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DependencyChainImpl) {
            DependencyChainImpl comparable = (DependencyChainImpl) obj;

            if (value.equals(comparable.value)) {
                return true;
            }

        }
        return false;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    //</editor-fold>
}
