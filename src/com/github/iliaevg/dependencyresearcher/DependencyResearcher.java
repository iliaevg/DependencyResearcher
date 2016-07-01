/*
 * Copyright 2016 ilaevg.
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
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ilaevg
 */
public class DependencyResearcher {

    //<editor-fold defaultstate="collapsed" desc="private static  methods">
    /**
     * Возвращает все возможные дочерние циклические цепочки зависимостей для
     * заданного корня цепочек. Рекурсивная.
     *
     * @return Список циклических зависимостей.
     */
    private static List<DependencyChain> getChildCycles(DependencyChain dependencyChain) throws DependencyResearcherException {

        List<DependencyChain> cycleDependenciesList = new ArrayList<>();
        // Выполняем проверки полученной ветки
        // Если ветка зациклилась на себя то вернем ее как результат
        if (dependencyChain.isCycled()) {
            cycleDependenciesList.add(dependencyChain);
            return cycleDependenciesList;
        }

        //  Если в ветке появились «паразитные» циклы то вернем пустой список
        //  Если какая-нибудь подпоследовательность зависимостей входит
        //  подряд два раза, то будем считать что есть цикл «паразит»
        if (dependencyChain.containSubCycles()) {
            return new ArrayList<>();
        }

        //  Если никаких циклов не обнаружено, переходим на уровень глубже
        List<Entity> nextLevelDepencies = dependencyChain.getDependenciesList();
        for (Entity nextDependency : nextLevelDepencies) {
            DependencyChain newDependencyChain = new DependencyChainImpl(dependencyChain);
            newDependencyChain.add(nextDependency);
            cycleDependenciesList.addAll(DependencyResearcher.getChildCycles(newDependencyChain)
            );
        }
        return cycleDependenciesList;
    }

    /**
     * Возвращает список циклических цепочек зависимостей без повторений
     *
     * @return
     */
    private static List<DependencyChain> removeDoubles(List<DependencyChain> cycles) {

        List<DependencyChain> cyclesWithoutDoubles = new ArrayList<>();

        for (DependencyChain cycle : cycles) {

            if (cycle.isCycled() == false) {
                break;
            }

            boolean newChainsListContainsCycle = false;
            for (DependencyChain matchedCycle : cyclesWithoutDoubles) {
                if (DependencyChainImpl.equalsAsCycles(cycle, matchedCycle)) {
                    newChainsListContainsCycle = true;
                    break;
                }
            }

            if (newChainsListContainsCycle == false) {
                cyclesWithoutDoubles.add(cycle);
            }
        }

        return cyclesWithoutDoubles;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="public static methods">
    /**
     * Разбирает входной поток от экземпляра класса Scanner и возвращает список
     * полученных сузщностей
     */
    public static List<Entity> parseSourceScanner(Scanner sourceScanner) throws DependencyResearcherException {

        //  Парсим вход и получаем список сущностей
        List<Entity> entitiesList = new ArrayList<>();
        while (sourceScanner.hasNextInt()) {

            //  Получаем следующую пару чисел
            int entityId = sourceScanner.nextInt();
            if (sourceScanner.hasNextInt() == false) {
                throw new DependencyResearcherException("Reading error: wrong format.");
            }
            int dependencyId = sourceScanner.nextInt();

            //  Получаем объекты типа Entity соответствующие полученным
            //  идентификаторам
            Entity currentEntity = null;
            Entity dependencyEntity = null;

            //  Если сущности уже заведены в программе то используем ссылку
            //  на них
            for (Entity entity : entitiesList) {
                if (entity.getId() == entityId) {
                    currentEntity = entity;
                }
                if (entity.getId() == dependencyId) {
                    dependencyEntity = entity;
                }
            }
            //  Если нет то создаем новые
            if (currentEntity == null) {
                currentEntity = new Entity(entityId);
                entitiesList.add(currentEntity);
            }
            if (dependencyEntity == null) {
                dependencyEntity = new Entity(dependencyId);
                entitiesList.add(dependencyEntity);
            }

            //  Прописываем прочитанную зависимость
            if (currentEntity.getDependenciesList().contains(dependencyEntity) == false) {
                currentEntity.getDependenciesList().add(dependencyEntity);
            }
        }

        return entitiesList;
    }

    /**
     * Возвращает циклические зависимости заданного списка сущностей
     *
     * @param entitiesList Список сущностей для которых нужно узнать циклические
     * зависимости
     * @return Список циклических зависимостей.
     */
    public static List<DependencyChain> getCycleDependencies(List<Entity> entitiesList) throws DependencyResearcherException {

        List<DependencyChain> cycleDependenciesList = new ArrayList<>();

        for (Entity entity : entitiesList) {
            for (Entity dependency : entity.getDependenciesList()) {
                DependencyChain baseChain = new DependencyChainImpl();
                baseChain.add(entity);
                baseChain.add(dependency);
                cycleDependenciesList.addAll(DependencyResearcher.getChildCycles(baseChain));
            }
        }

        return DependencyResearcher.removeDoubles(cycleDependenciesList);

    }

    public static String getDependencyCyclesPrintView(List<DependencyChain> dependencyCycles) {

        List<DependencyChain> cyclesToPrint = new ArrayList<>(dependencyCycles);
        Collections.sort(cyclesToPrint);
        StringBuilder stringBuilder = new StringBuilder();

        for (DependencyChain dependencyCycle : cyclesToPrint) {
            stringBuilder.append(dependencyCycle.formattedView());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="main">
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Scanner sourceScanner = null;

            if (args.length == 0) {

                sourceScanner = new Scanner(System.in);
            } else if (args.length == 1) {

                try {
                    File file = new File(args[0]);
                    sourceScanner = new Scanner(file);
                } catch (Exception exception) {
                    throw new DependencyResearcherException("Error reading data file\n" + args[0], exception);
                }
            } else {

                throw new DependencyResearcherException("Incorrect number of arguments");
            }

            List<Entity> entiities = DependencyResearcher.parseSourceScanner(sourceScanner);
            List<DependencyChain> cycleDependencies = DependencyResearcher.getCycleDependencies(entiities);
            System.out.println(DependencyResearcher.getDependencyCyclesPrintView(cycleDependencies));

        } catch (DependencyResearcherException ex) {
            Logger.getLogger(DependencyResearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//</editor-fold>
}
