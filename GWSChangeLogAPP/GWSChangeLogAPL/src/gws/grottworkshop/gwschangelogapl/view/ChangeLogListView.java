package gws.grottworkshop.gwschangelogapl.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import gws.grottworkshop.gwschangelogapl.R;
import gws.grottworkshop.gwschangelogapl.model.ChangeLogAdapterModel;
import gws.grottworkshop.gwschangelogapl.model.ChangeLogModel;
import gws.grottworkshop.gwschangelogapl.model.ChangeLogRowModel;
import gws.grottworkshop.gwschangelogappl.Constants;
import gws.grottworkshop.gwschangelogappl.Util;


public class ChangeLogListView extends ListView implements AdapterView.OnItemClickListener {

    //--------------------------------------------------------------------------
    // Custom Attrs
    //--------------------------------------------------------------------------
    protected int mRowLayoutId= Constants.mRowLayoutId;
    protected int mRowHeaderLayoutId=Constants.mRowHeaderLayoutId;
    protected int mChangeLogFileResourceId=Constants.mChangeLogFileResourceId;
    protected String mChangeLogFileResourceUrl=null;

    //--------------------------------------------------------------------------
    protected static String TAG="ChangeLogListView";
    // Adapter
    protected ChangeLogAdapterModel mAdapter;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public ChangeLogListView(Context context) {
        super(context);
        init(null, 0);
    }

    public ChangeLogListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs,0);
    }

    public ChangeLogListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs,defStyle);
    }

    //--------------------------------------------------------------------------
    // Init
    //--------------------------------------------------------------------------

    /**
     * Initialize
     *
     * @param attrs
     * @param defStyle
     */
    protected void init(AttributeSet attrs, int defStyle){
        //Init attrs
        initAttrs(attrs,defStyle);
        //Init adapter
        initAdapter();

        //Set divider to 0dp
        setDividerHeight(0);
    }

    /**
     * Init custom attrs.
     *
     * @param attrs
     * @param defStyle
     */
    protected void initAttrs(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.ChangeLogListView, defStyle, defStyle);

        try {
            //Layout for rows and header
            mRowLayoutId = a.getResourceId(R.styleable.ChangeLogListView_rowLayoutId, mRowLayoutId);
            mRowHeaderLayoutId = a.getResourceId(R.styleable.ChangeLogListView_rowHeaderLayoutId, mRowHeaderLayoutId);

            //Changelog.xml file
            mChangeLogFileResourceId = a.getResourceId(R.styleable.ChangeLogListView_changeLogFileResourceId,mChangeLogFileResourceId);

            mChangeLogFileResourceUrl = a.getString(R.styleable.ChangeLogListView_changeLogFileResourceUrl);
            //String which is used in header row for Version
            //mStringVersionHeader= a.getResourceId(R.styleable.ChangeLogListView_StringVersionHeader,mStringVersionHeader);

        } finally {
            a.recycle();
        }
    }

    /**
     * Init adapter
     */
    protected void initAdapter() {

        try{
            //Read and parse changelog.xml
            XmlParser parse;
            if (mChangeLogFileResourceUrl!=null)
                parse = new XmlParser(getContext(),mChangeLogFileResourceUrl);
            else
                parse = new XmlParser(getContext(),mChangeLogFileResourceId);
            //ChangeLog chg=parse.readChangeLogFile();
            ChangeLogModel chg = new ChangeLogModel();

            if (chg!=null){
                //Create adapter and set custom attrs
                mAdapter = new ChangeLogAdapterModel(getContext(),chg.getRows());
                mAdapter.setmRowLayoutId(mRowLayoutId);
                mAdapter.setmRowHeaderLayoutId(mRowHeaderLayoutId);

                //Parse in a separate Thread to avoid UI block with large files
                if (mChangeLogFileResourceUrl==null || (mChangeLogFileResourceUrl!=null && Util.isConnected(getContext())))
                    new ParseAsyncTask(mAdapter,parse).execute();
                else
                    Toast.makeText(getContext(),R.string.changelog_internal_error_internet_connection,Toast.LENGTH_LONG).show();
                setAdapter(mAdapter);
            }else{
                setAdapter(null);
            }
        }catch (Exception e){
            Log.e(TAG,getResources().getString(R.string.changelog_internal_error_parsing),e);
        }

    }

    /**
     * Async Task to parse xml file in a separate thread
     *
     */
    protected class ParseAsyncTask extends AsyncTask<Void, Void, ChangeLogModel>{

        private ChangeLogAdapterModel mAdapter;
        private XmlParser mParse;

        public ParseAsyncTask(ChangeLogAdapterModel adapter,XmlParser parse){
            mAdapter=adapter;
            mParse= parse;
        }

        @Override
        protected ChangeLogModel doInBackground(Void... params) {

            try{
                if (mParse!=null){
                    ChangeLogModel chg=mParse.readChangeLogFile();
                    return chg;
                }
            }catch (Exception e){
                Log.e(TAG,getResources().getString(R.string.changelog_internal_error_parsing),e);
            }
            return null;
        }

        protected void onPostExecute(ChangeLogModel chg) {

            //Notify data changed
            if (chg!=null){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                    mAdapter.addAll(chg.getRows());
                }else{
                    if(chg.getRows()!=null){
                        for(ChangeLogRowModel row:chg.getRows()){
                            mAdapter.add(row);
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    }


    /**
     * Sets the list's adapter, enforces the use of only a ChangeLogAdapter
     */
    public void setAdapter(ChangeLogAdapterModel adapter) {
        super.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //TODO
    }


}
