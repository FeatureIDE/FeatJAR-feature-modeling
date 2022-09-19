/*
 * Copyright (C) 2022 Elias Kuiter
 *
 * This file is part of model.
 *
 * model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-model> for further information.
 */
package de.featjar.feature.model;

import de.featjar.feature.model.util.*;
import de.featjar.formula.structure.Expression;
import de.featjar.formula.tmp.Formulas;
import de.featjar.feature.model.mixins.CommonAttributesMixin;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A constraint describes some restriction of the valid configurations
 * represented by a {@link FeatureModel}. It is attached to some feature model
 * and represented as a {@link Expression} over {@link Feature features}. For safe
 * mutation, rely only on the methods of {@link Mutable}.
 *
 * @author Elias Kuiter
 */
public class Constraint extends Element
        implements Mutable<Constraint, Constraint.Mutator>, Analyzable<Constraint, Constraint.Analyzer> {
    protected final FeatureModel featureModel;
    protected Expression expression;
    protected final Set<Feature> containedFeaturesCache = new HashSet<>();
    protected Mutator mutator;
    protected Analyzer analyzer;

    public Constraint(FeatureModel featureModel, Expression expression) {
        super(featureModel.getNewIdentifier());
        Objects.requireNonNull(featureModel);
        this.featureModel = featureModel;
        getMutator().setFormula(expression); // todo efficient?
    }

    public Constraint(FeatureModel featureModel) {
        super(featureModel.getNewIdentifier());
        Objects.requireNonNull(featureModel);
        this.featureModel = featureModel;
        this.expression = Expression.TRUE;
    }

    public FeatureModel getFeatureModel() {
        return featureModel;
    }

    public Expression getFormula() {
        return expression;
    }

    public Set<Feature> getContainedFeatures() {
        return containedFeaturesCache;
    }

    public Set<String> getTags() {
        return getAttributeValue(Attributes.TAGS);
    }

    @Override
    public Mutator getMutator() {
        return mutator == null ? (mutator = new Mutator()) : mutator;
    }

    @Override
    public void setMutator(Mutator mutator) {
        this.mutator = mutator;
    }

    @Override
    public String toString() {
        return String.format("Constraint{formula=%s}", expression);
    }

    @Override
    public Analyzer getAnalyzer() {
        return analyzer == null ? (analyzer = new Analyzer()) : analyzer;
    }

    @Override
    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public class Mutator
            implements de.featjar.feature.model.util.Mutator<Constraint>, CommonAttributesMixin.Mutator<Constraint> {
        @Override
        public Constraint getMutable() {
            return Constraint.this;
        }

        public void setFormula(Expression expression) {
            Objects.requireNonNull(expression);
            Set<Identifier> identifiers = Formulas.getVariableNames(expression).stream()
                    .map(getIdentifier().getFactory()::parse)
                    .collect(Collectors.toSet());
            Optional<Identifier> unknownIdentifier = identifiers.stream()
                    .filter(identifier -> !featureModel.hasFeature(identifier))
                    .findAny();
            if (unknownIdentifier.isPresent()) {
                throw new RuntimeException("encountered unknown identifier " + unknownIdentifier.get());
            }
            containedFeaturesCache.clear();
            containedFeaturesCache.addAll(identifiers.stream()
                    .map(featureModel::getFeature)
                    .map(Optional::get)
                    .collect(Collectors.toSet()));
            Constraint.this.expression = expression;
        }

        public void setTags(Set<String> tags) {
            setAttributeValue(Attributes.TAGS, tags);
        }

        public void remove() {
            getFeatureModel().mutate().removeConstraint(Constraint.this);
        }
    }

    public class Analyzer implements de.featjar.feature.model.util.Analyzer<Constraint> {
        @Override
        public Constraint getAnalyzable() {
            return Constraint.this;
        }

        public boolean isRedundant() {
            return false;
        }

        // ...
    }
}