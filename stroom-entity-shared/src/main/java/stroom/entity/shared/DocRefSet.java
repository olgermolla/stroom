/*
 * Copyright 2016 Crown Copyright
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

package stroom.entity.shared;

import stroom.query.api.v2.DocRef;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Hold id criteria
 */
@XmlRootElement(name = "docRefs")
public class DocRefSet extends CriteriaSet<String> {
    private static final long serialVersionUID = 1L;

    public DocRefSet() {
        super(new TreeSet<>());
    }

    public DocRefSet(final Set<DocRef> es) {
        super(new TreeSet<>());
        for (final DocRef e : es) {
            add(e);
        }
    }

    @Override
    @XmlTransient
    public Set<String> getSet() {
        return super.getSet();
    }

    @Override
    public void setSet(final Set<String> set) {
        super.setSet(set);
    }

    /**
     * THIS IS HERE ONLY FOR BACKWARD COMPATIBILITY WITH OLD SERIALISED VERSIONS
     * <p>
     * DO NOT USE BUT DO NOT REMOVE EITHER
     */
    @Deprecated
    public List<String> getIdSet() {
        return null;
    }

    /**
     * THIS IS HERE ONLY FOR BACKWARD COMPATIBILITY WITH OLD SERIALISED VERSIONS
     * <p>
     * DO NOT USE BUT DO NOT REMOVE EITHER
     */
    @Deprecated
    public void setIdSet(final List<String> newSet) {
        setUuid(newSet);
    }

    /**
     * HERE FOR XML JAXB serialisation ..... DO NOT REMOVE
     */
    public Collection<String> getUuid() {
        return getSet();
    }

    /**
     * HERE FOR XML JAXB serialisation ..... DO NOT REMOVE
     */
    public void setUuid(final Collection<String> newSet) {
        if (newSet == null) {
            setSet(null);
        } else {
            setSet(new TreeSet<>(newSet));
        }
    }

//    public String getMaxId() {
//        String max = null;
//        for (final String id : getSet()) {
//            if (max == null) {
//                max = id;
//            } else {
//                if (id.longValue() > max.longValue()) {
//                    max = id;
//                }
//            }
//        }
//        return max;
//    }

    /**
     * @param id update so as not to change JAXB
     */
    public void updateSingleId(final String id) {
        clear();
        if (id != null) {
            add(id);
        }
    }

    public String getSingleId() {
        if (!isConstrained()) {
            return null;
        }
        if (getSet().size() != 1) {
            throw new RuntimeException("Single is state invalid");
        }
        return getSet().iterator().next();
    }

    public void add(final DocRef e) {
        super.add(e.getUuid());
    }

    public void remove(final DocRef e) {
        super.remove(e.getUuid());
    }

    public void addAllEntities(final Collection<DocRef> s) {
        if (s != null) {
            for (final DocRef t : s) {
                add(t);
            }
        }
    }

    public boolean isMatch(final DocRef e) {
        if (e == null) {
            return isMatch((String) null);
        } else {
            return isMatch(e.getUuid());
        }
    }
}
