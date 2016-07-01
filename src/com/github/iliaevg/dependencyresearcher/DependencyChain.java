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
import java.util.List;

/**
 *
 * @author iliaevg
 */
public interface DependencyChain extends Iterable<Entity>, Comparable<DependencyChain> {

    /**
     * Добавляет к цепочке зависимостей следующее звено
     *
     * @throws
     * com.github.iliaevg.dependencyresearcher.exception.DependencyResearcherException
     */
    boolean add(Entity e) throws DependencyResearcherException;

    /**
     * добавляет к текущей цепочке заданную цепочку
     */
    void addDependencyChain(DependencyChainImpl dc) throws DependencyResearcherException;

    /**
     * Определяет, есть ли в цепочке подциклы
     */
    boolean containSubCycles();
    
    String formattedView();

    /**
     * Возвращает список сущностей, зависящих от последнего элемента цепочкис
     */
    List<Entity> getDependenciesList();

    /**
     * Определяет, зациклена ли цепочкаs
     */
    boolean isCycled();

    /**
     * Возвращает длину цепочки зависимостей
     */
    int size();

    /**
     * Возвращает цепочку зависимостей в виде списка сущностейЫ
     */
    List<Entity> getListValue();
    
}
