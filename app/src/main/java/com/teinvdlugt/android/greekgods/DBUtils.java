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
     * Format this String using one argument, being the id of the person
     */
    public static final String PARENTS_QUERY = "SELECT p.name\n" +
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

}
