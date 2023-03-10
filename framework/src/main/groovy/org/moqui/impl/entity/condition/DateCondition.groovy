/*
 * This software is in the public domain under CC0 1.0 Universal plus a 
 * Grant of Patent License.
 * 
 * To the extent possible under law, the author(s) have dedicated all
 * copyright and related and neighboring rights to this software to the
 * public domain worldwide. This software is distributed without any
 * warranty.
 * 
 * You should have received a copy of the CC0 Public Domain Dedication
 * along with this software (see the LICENSE.md file). If not, see
 * <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package org.moqui.impl.entity.condition

import groovy.transform.CompileStatic
import org.moqui.entity.EntityCondition
import org.moqui.entity.EntityException
import org.moqui.impl.entity.EntityDefinition

import java.sql.Timestamp
import org.moqui.impl.entity.EntityQueryBuilder

@CompileStatic
class DateCondition implements EntityConditionImplBase, Externalizable {
    protected ConditionField fromField
    protected ConditionField thruField
    protected Timestamp compareStamp
    private EntityConditionImplBase conditionInternal
    private int hashCodeInternal

    DateCondition(ConditionField fromField, ConditionField thruField, Timestamp compareStamp) {
        this.fromField = fromField
        this.thruField = thruField
        this.compareStamp = compareStamp
        conditionInternal = makeConditionInternal()
        hashCodeInternal = createHashCode()
    }
    DateCondition(String fromFieldName, String thruFieldName, Timestamp compareStamp) {
        this.fromField = new ConditionField(fromFieldName ?: "fromDate")
        this.thruField = new ConditionField(thruFieldName ?: "thruDate")
        if (compareStamp == (Timestamp) null) compareStamp = new Timestamp(System.currentTimeMillis())
        this.compareStamp = compareStamp
        conditionInternal = makeConditionInternal()
        hashCodeInternal = createHashCode()
    }

    @Override void makeSqlWhere(EntityQueryBuilder eqb, EntityDefinition subMemberEd) {
        conditionInternal.makeSqlWhere(eqb, subMemberEd) }
    @Override void makeSearchFilter(List<Map<String, Object>> filterList) {
        conditionInternal.makeSearchFilter(filterList) }

    @Override
    void getAllAliases(Set<String> entityAliasSet, Set<String> fieldAliasSet) {
        if (fromField instanceof ConditionAlias) {
            entityAliasSet.add(((ConditionAlias) fromField).entityAlias)
        } else {
            fieldAliasSet.add(fromField.fieldName)
        }
        if (thruField instanceof ConditionAlias) {
            entityAliasSet.add(((ConditionAlias) thruField).entityAlias)
        } else {
            fieldAliasSet.add(thruField.fieldName)
        }
    }
    @Override EntityConditionImplBase filter(String entityAlias, EntityDefinition mainEd) { return conditionInternal.filter(entityAlias, mainEd) }

    @Override boolean mapMatches(Map<String, Object> map) { return conditionInternal.mapMatches(map) }
    @Override boolean mapMatchesAny(Map<String, Object> map) { return conditionInternal.mapMatchesAny(map) }
    @Override boolean mapKeysNotContained(Map<String, Object> map) { return conditionInternal.mapKeysNotContained(map) }

    @Override boolean populateMap(Map<String, Object> map) { return false }

    @Override EntityCondition ignoreCase() { throw new EntityException("Ignore case not supported for DateCondition.") }

    @Override String toString() { return conditionInternal.toString() }

    private EntityConditionImplBase makeConditionInternal() {
        return new ListCondition([
            new ListCondition([new FieldValueCondition(fromField, EQUALS, null),
                               new FieldValueCondition(fromField, LESS_THAN_EQUAL_TO, compareStamp)] as List<EntityConditionImplBase>,
                    EntityCondition.JoinOperator.OR),
            new ListCondition([new FieldValueCondition(thruField, EQUALS, null),
                               new FieldValueCondition(thruField, GREATER_THAN, compareStamp)] as List<EntityConditionImplBase>,
                    EntityCondition.JoinOperator.OR)
        ] as List<EntityConditionImplBase>, EntityCondition.JoinOperator.AND)
    }

    @Override int hashCode() { return hashCodeInternal }
    private int createHashCode() { return compareStamp.hashCode() + fromField.hashCode() + thruField.hashCode() }

    @Override
    boolean equals(Object o) {
        if (o == null || o.getClass() != this.getClass()) return false
        DateCondition that = (DateCondition) o
        if (!this.compareStamp.equals(that.compareStamp)) return false
        if (!fromField.equals(that.fromField)) return false
        if (!thruField.equals(that.thruField)) return false
        return true
    }

    @Override
    void writeExternal(ObjectOutput out) throws IOException {
        fromField.writeExternal(out)
        thruField.writeExternal(out)
        out.writeLong(compareStamp.getTime())
    }
    @Override
    void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        fromField = new ConditionField()
        fromField.readExternal(objectInput)
        thruField = new ConditionField()
        thruField.readExternal(objectInput)
        compareStamp = new Timestamp(objectInput.readLong());

        hashCodeInternal = createHashCode();
        conditionInternal = makeConditionInternal();
    }
}
