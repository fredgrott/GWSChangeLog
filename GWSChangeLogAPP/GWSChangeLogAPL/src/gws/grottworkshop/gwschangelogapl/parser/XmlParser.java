/*******************************************************************************
 * Copyright (c) 2013 Gabriele Mariotti.
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
package gws.grottworkshop.gwschangelogapl.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import gws.grottworkshop.gwschangelogapl.model.ChangeLogException;
import gws.grottworkshop.gwschangelogapl.model.ChangeLogModel;
import gws.grottworkshop.gwschangelogapl.model.ChangeLogRowHeaderModel;
import gws.grottworkshop.gwschangelogapl.model.ChangeLogRowModel;
import gws.grottworkshop.gwschangelogappl.Constants;
import gws.grottworkshop.gwschangelogappl.Util;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

/**
 * Read and parse res/raw/changelog.xml.
 * Example:
 *
 * <pre>
 *    XmlParser parse = new XmlParser(this);
 *    ChangeLog log=parse.readChangeLogFile();
 * </pre>
 *
 * If you want to use a custom xml file, you can use:
 * <pre>
 *    XmlParser parse = new XmlParser(this,R.raw.mycustomfile);
 *    ChangeLog log=parse.readChangeLogFile();
 * </pre>
 *
 * It is a example for changelog.xml
 * <pre>
 *  <?xml version="1.0" encoding="utf-8"?>
 *       <changelog bulletedList=false>
 *            <changelogversion versionName="1.2" changeDate="20/01/2013">
 *                 <changelogtext>new feature to share data</changelogtext>
 *                 <changelogtext>performance improvement</changelogtext>
 *            </changelogversion>
 *            <changelogversion versionName="1.1" changeDate="13/01/2013">
 *                 <changelogtext>issue on wifi connection</changelogtext>*
 *            </changelogversion>*
 *       </changelog>
 * </pre>
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 *
 */
public class XmlParser extends BaseParser {

    /** TAG for logging **/
    private static String TAG="XmlParser";
    private int mChangeLogFileResourceId= Constants.mChangeLogFileResourceId;
    private String mChangeLogFileResourceUrl= null;

    //--------------------------------------------------------------------------------
    //TAGs and ATTRIBUTEs in xml file
    //--------------------------------------------------------------------------------

    private static final String TAG_CHANGELOG="changelog";
    private static final String TAG_CHANGELOGVERSION="changelogversion";
    private static final String TAG_CHANGELOGTEXT="changelogtext";

    private static final String ATTRIBUTE_BULLETEDLIST="bulletedList";
    private static final String ATTRIBUTE_VERSIONNAME="versionName";
    private static final String ATTRIBUTE_CHANGEDATE="changeDate";
    private static final String ATTRIBUTE_CHANGETEXT="changeText";
    private static final String ATTRIBUTE_CHANGETEXTTITLE= "changeTextTitle";

    //--------------------------------------------------------------------------------
    //Constructors
    //--------------------------------------------------------------------------------

    /**
     * Create a new instance for a context.
     *
     * @param context  current Context
     */
    public XmlParser(Context context){
        super(context);
    }

    /**
     * Create a new instance for a context and for a custom changelogfile.
     *
     * You have to use file in res/raw folder.
     *
     * @param context  current Context
     * @param changeLogFileResourceId  reference for a custom xml file
     */
    public XmlParser(Context context,int changeLogFileResourceId){
        super(context);
        this.mChangeLogFileResourceId=changeLogFileResourceId;
    }

    /**
     * Create a new instance for a context and with a custom url .
     *
     * @param context  current Context
     * @param changeLogFileResourceUrl  url with xml files
     */
    public XmlParser(Context context,String changeLogFileResourceUrl){
        super(context);
        this.mChangeLogFileResourceUrl=changeLogFileResourceUrl;
    }

    //--------------------------------------------------------------------------------


    /**
     * Read and parse res/raw/changelog.xml or custom file
     *
     * @throws Exception if changelog.xml or custom file is not found or if there are errors on parsing
     *
     * @return {@link ChangeLog} obj with all data
     */
    @Override
    public ChangeLogModel readChangeLogFile() throws Exception{

        ChangeLogModel chg=null;

        try {
            InputStream is=null;

            if (mChangeLogFileResourceUrl!=null){
                if (Util.isConnected(super.mContext)){
                    URL url = new URL(mChangeLogFileResourceUrl);
                    is = url.openStream();
                }
            }else{
                is = mContext.getResources().openRawResource(mChangeLogFileResourceId);
            }
            if (is!=null){

                // Create a new XML Pull Parser.
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(is, null);
                parser.nextTag();

                // Create changelog obj that will contain all data
                chg=new ChangeLogModel();
                // Parse file
                readChangeLogNode(parser, chg);

                // Close inputstream
                is.close();
            }else{
                Log.d(TAG,"Changelog.xml not found");
                throw new ChangeLogException("Changelog.xml not found");
            }
        } catch (XmlPullParserException xpe) {
            Log.d(TAG,"XmlPullParseException while parsing changelog file",xpe);
            throw  xpe;
        } catch (IOException ioe){
            Log.d(TAG,"Error i/o with changelog.xml",ioe);
            throw ioe;
        }

        if (chg!=null)
            Log.d(TAG,"Process ended. ChangeLog:"+chg.toString());

        return chg;
    }


    /**
     * Parse changelog node
     *
     * @param parser
     * @param changeLog
     */
    protected void readChangeLogNode(XmlPullParser parser,ChangeLogModel changeLog) throws Exception{

        if (parser==null || changeLog==null) return;

        // Parse changelog node
        parser.require(XmlPullParser.START_TAG, null,TAG_CHANGELOG);
        Log.d(TAG,"Processing main tag=");

        // Read attributes
        String bulletedList = parser.getAttributeValue(null, ATTRIBUTE_BULLETEDLIST);
        if (bulletedList==null || bulletedList.equals("true")){
            changeLog.setBulletedList(true);
            super.bulletedList=true;
        }else{
            changeLog.setBulletedList(false);
            super.bulletedList=false;
        }

        //Parse nested nodes
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tag = parser.getName();
            Log.d(TAG,"Processing tag="+tag);

            if (tag.equals(TAG_CHANGELOGVERSION)) {
                readChangeLogVersionNode(parser, changeLog);
            }
        }
    }

    /**
     * Parse changeLogVersion node
     *
     * @param parser
     * @param changeLog
     * @throws Exception
     */
    protected void readChangeLogVersionNode(XmlPullParser parser, ChangeLogModel changeLog) throws  Exception{

        if (parser==null) return;

        parser.require(XmlPullParser.START_TAG, null,TAG_CHANGELOGVERSION);

        // Read attributes
        String versionName = parser.getAttributeValue(null, ATTRIBUTE_VERSIONNAME);
        String changeDate= parser.getAttributeValue(null, ATTRIBUTE_CHANGEDATE);
        if (versionName==null)
            throw new ChangeLogException("VersionName required in changeLogVersion node");

        ChangeLogRowHeaderModel row=new ChangeLogRowHeaderModel();
        row.setVersionName(versionName);
        row.setChangeDate(changeDate);
        changeLog.addRow(row);

        Log.d(TAG,"Added rowHeader:"+row.toString());

        // Parse nested nodes
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag = parser.getName();
            Log.d(TAG,"Processing tag="+tag);

            if (tag.equals(TAG_CHANGELOGTEXT)){
                readChangeLogRowNode(parser, changeLog,versionName);
            }
        }
    }

    /**
     *  Parse changeLogText node
     *
     * @param parser
     * @param changeLog
     * @throws Exception
     */
    private void readChangeLogRowNode(XmlPullParser parser, ChangeLogModel changeLog,String versionName) throws  Exception{

        if (parser==null) return;

        parser.require(XmlPullParser.START_TAG, null,TAG_CHANGELOGTEXT);

        String tag = parser.getName();
        if (tag.equals(TAG_CHANGELOGTEXT)){
            ChangeLogRowModel row=new ChangeLogRowModel();
            row.setVersionName(versionName);

            // Read attributes
            String changeLogTextTitle=parser.getAttributeValue(null,ATTRIBUTE_CHANGETEXTTITLE);
            if (changeLogTextTitle!=null)
                row.setChangeTextTitle(changeLogTextTitle);

            // It is possible to force bulleted List
            String bulletedList = parser.getAttributeValue(null, ATTRIBUTE_BULLETEDLIST);
            if (bulletedList!=null){
                if (bulletedList.equals("true")){
                    row.setBulletedList(true);
                }else{
                    row.setBulletedList(false);
                }
            }else{
                row.setBulletedList(super.bulletedList);
            }

            // Read text
            if (parser.next() == XmlPullParser.TEXT) {
                String changeLogText=parser.getText();
                if (changeLogText==null)
                    throw new ChangeLogException("ChangeLogText required in changeLogText node");
                row.parseChangeText(changeLogText);
                parser.nextTag();
            }
            changeLog.addRow(row);

            Log.d(TAG,"Added row:"+row.toString());
        }
        parser.require(XmlPullParser.END_TAG, null,TAG_CHANGELOGTEXT);
    }

}
