/* Greek Gods: an Android application which shows the family tree of the Greek Gods.
 * Copyright (C) 2016 Tein van der Lugt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.teinvdlugt.android.greekgods;

public class DBUtils {
    /**
     * Format this String using one argument, being the id
     * of the person you want to know the parents of
     */
    public static final String PARENTS_QUERY = "SELECT p.personId, p.name\n" +
            "FROM people p\n" +
            "WHERE (p.personId IN (\n" +
            "  SELECT r.personId1\n" +
            "  FROM relations r\n" +
            "  WHERE r.relatiod_id = (\n" +
            "    SELECT b.relationId\n" +
            "    FROM births b\n" +
            "    WHERE b.personId = %1$d\n" +
            "  )) OR (p.personId IN (\n" +
            "    SELECT r.personId2\n" +
            "    FROM relations r\n" +
            "    WHERE r.relatiod_id = (\n" +
            "      SELECT b.relationId\n" +
            "      FROM births b\n" +
            "      WHERE b.personId = %1$d\n" +
            "    )\n" +
            "  ))\n" +
            ")";

    /**
     * Format this String using one argument, being the id of
     * the person you want to know the relations of
     */
    public static final String RELATIONS_OF_PERSON_QUERY = "SELECT\n" +
            "  p.personId,\n" +
            "  p.name,\n" +
            "  r.relatiod_id\n" +
            "FROM people p, relations r\n" +
            "WHERE (r.personId1 = %1$d AND p.personId = r.personId2)\n" +
            "      OR (r.personId2 = %1$d AND p.personId = r.personId1)\n" +
            "      OR (((r.personId1 IS NULL AND r.personId2 = %1$d)\n" +
            "           OR r.personId2 IS NULL AND r.personId1 = %1$d) AND p.personId = %1$d)";

    /**
     * Format this String using one argument, being the relationId of the relation
     * you want to get the children from
     */
    public static final String BIRTHS_FROM_RELATION_QUERY = "SELECT p.personId, p.name\n" +
            "FROM people p, births b\n" +
            "WHERE b.relationId = %1$d AND p.personId = b.personId";

    /**
     * Format this String using one argument, being the personId of the person
     * you want to get the parent relations of.
     */
    public static final String PARENTS_RELATIONS_QUERY = "SELECT b.relationId\n" +
            "FROM births b\n" +
            "WHERE b.personId = %d";

    /**
     * Format this String using one argument, being a relationId.
     */
    public static final String NAMES_OF_RELATION_QUERY = "SELECT p.name\n" +
            "FROM people p, relations r\n" +
            "WHERE r.relatiod_id = %d AND (\n" +
            "  p.personId = r.personId1 OR\n" +
            "  p.personId = r.personId2\n" +
            ")";

    public static final String NAMES_OF_TWO_PEOPLE_QUERY = "SELECT p.personId, p.name\n" +
            "FROM people p\n" +
            "WHERE p.personId = %1$d OR p.personId = %2$d";

}
