/*******************************************************************************
 * Copyright (c) 2013 Gabriele Mariotti., Modifications by Fred Grott(GrottWorkShop)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package gws.grottworkshop.gwschangelogapl.model;

import java.util.LinkedList;

public class ChangeLogModel {


    /**
     * All changelog rows
     */
    private LinkedList<ChangeLogRowModel> rows;

    /**
     * Use a bulleted List
     */
    private boolean bulletedList;

    //-----------------------------------------------------------------------

    public ChangeLogModel(){
        rows=new LinkedList<ChangeLogRowModel>();
    }

    /**
     * Add new {@link ChangeLogRow} to rows
     *
     * @param row
     */
    public void addRow(ChangeLogRowModel row){
        if (row!=null){
            if (rows==null) rows=new LinkedList<ChangeLogRowModel>();
            rows.add(row);
        }
    }

    /**
     * Clear all rows
     */
    public void clearAllRows(){
        rows=new LinkedList<ChangeLogRowModel>();
    }


    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append("bulletedList="+bulletedList);
        sb.append("\n");
        if (rows!=null){
            for (ChangeLogRowModel row:rows){
                sb.append("row=[");
                sb.append(row.toString());
                sb.append("]\n");
            }
        }else{
            sb.append("rows:none");
        }
        return sb.toString();
    }

    //-----------------------------------------------------------------------

    public boolean isBulletedList() {
        return bulletedList;
    }

    public void setBulletedList(boolean bulletedList) {
        this.bulletedList = bulletedList;
    }

    public LinkedList<ChangeLogRowModel> getRows() {
        return rows;
    }

    public void setRows(LinkedList<ChangeLogRowModel> rows) {
        this.rows = rows;
    }





}
