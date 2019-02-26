/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.meta.shared;

import stroom.docref.SharedObject;
import stroom.util.shared.Copyable;
import stroom.util.shared.HasIsConstrained;
import stroom.util.shared.IdSet;
import stroom.util.shared.PageRequest;
import stroom.util.shared.Sort;
import stroom.util.shared.Sort.Direction;
import stroom.query.api.v2.ExpressionOperator;

import java.util.ArrayList;
import java.util.List;

public class FindMetaCriteria implements SharedObject, HasIsConstrained, Copyable<FindMetaCriteria> {
    private static final long serialVersionUID = -4777723504698304778L;

    private ExpressionOperator expression;
    private IdSet selectedIdSet;
    private PageRequest pageRequest = null;
    private List<Sort> sortList;

    public FindMetaCriteria() {
    }

    public FindMetaCriteria(final ExpressionOperator expression) {
        this.expression = expression;
    }

    public static FindMetaCriteria createFromMeta(final Meta meta) {
        final FindMetaCriteria criteria = new FindMetaCriteria();
        criteria.setExpression(ExpressionUtil.createSimpleExpression());
        criteria.obtainSelectedIdSet().add(meta.getId());
        return criteria;
    }

    public static FindMetaCriteria createWithType(final String typeName) {
        final FindMetaCriteria criteria = new FindMetaCriteria();
        criteria.setExpression(ExpressionUtil.createTypeExpression(typeName));
        return criteria;
    }

    public ExpressionOperator getExpression() {
        return expression;
    }

    public void setExpression(final ExpressionOperator expression) {
        this.expression = expression;
    }

    public ExpressionOperator obtainExpression() {
        if (expression == null) {
            expression = ExpressionUtil.createSimpleExpression();
        }
        return expression;
    }

    public IdSet getSelectedIdSet() {
        return selectedIdSet;
    }

    public void setSelectedIdSet(final IdSet selectedIdSet) {
        this.selectedIdSet = selectedIdSet;
    }

    public IdSet obtainSelectedIdSet() {
        if (selectedIdSet == null) {
            selectedIdSet = new IdSet();
        }
        return selectedIdSet;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }

    public void setPageRequest(final PageRequest pageRequest) {
        this.pageRequest = pageRequest;
    }

    public PageRequest obtainPageRequest() {
        if (pageRequest == null) {
            pageRequest = new PageRequest();
        }
        return pageRequest;
    }

    public void setSort(final String field) {
        setSort(new Sort(field, Direction.ASCENDING, false));
    }

    public void setSort(final String field, final Direction direction, final boolean ignoreCase) {
        setSort(new Sort(field, direction, ignoreCase));
    }

    public void setSort(final Sort sort) {
        sortList = null;
        addSort(sort);
    }

    public void addSort(final String field) {
        addSort(new Sort(field, Direction.ASCENDING, false));
    }

    public void addSort(final String field, final Direction direction, final boolean ignoreCase) {
        addSort(new Sort(field, direction, ignoreCase));
    }

    public void addSort(final Sort sort) {
        if (sortList == null) {
            sortList = new ArrayList<>();
        }
        sortList.add(sort);
    }

    public List<Sort> getSortList() {
        return sortList;
    }

    @Override
    public boolean isConstrained() {
        return (selectedIdSet != null && selectedIdSet.isConstrained()) || ExpressionUtil.termCount(expression) > 0;
    }

    @Override
    public void copyFrom(final FindMetaCriteria other) {
        if (other.pageRequest == null) {
            this.pageRequest = null;
        } else {
            this.obtainPageRequest().copyFrom(other.pageRequest);
        }
        if (other.sortList == null) {
            this.sortList = null;
        } else {
            this.sortList = new ArrayList<>(other.sortList);
        }
        this.expression = ExpressionUtil.copyOperator(other.expression);
        if (other.selectedIdSet == null) {
            this.selectedIdSet = null;
        } else {
            this.obtainSelectedIdSet().copyFrom(other.selectedIdSet);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FindMetaCriteria)) return false;

        final FindMetaCriteria that = (FindMetaCriteria) o;

        if (pageRequest != null ? !pageRequest.equals(that.pageRequest) : that.pageRequest != null) return false;
        return sortList != null ? sortList.equals(that.sortList) : that.sortList == null;
    }

    @Override
    public int hashCode() {
        int result = pageRequest != null ? pageRequest.hashCode() : 0;
        result = 31 * result + (sortList != null ? sortList.hashCode() : 0);
        return result;
    }
}