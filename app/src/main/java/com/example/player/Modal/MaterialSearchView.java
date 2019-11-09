package com.example.player.Modal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.player.OnFileListener;
import com.example.player.R;
import com.example.player.VideoPlayerActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MaterialSearchView extends FrameLayout implements OnFileListener {
    public static final int REQUEST_VOICE = 9999;

    private MenuItem mMenuItem;
    private boolean mIsSearchOpen = false;
    private int mAnimationDuration;
    private boolean mClearingFocus;

    //Views
    private View mSearchLayout;
    private View mTintView;
    private RecyclerView mSuggestionsRecyclerView;
    private EditText mSearchSrcTextView;
    private ImageButton mBackBtn;
    private ImageButton mVoiceBtn;
    private ImageButton mEmptyBtn;
    private RelativeLayout mSearchTopBar;
    private TextView noResultFound;

    private CharSequence mUserQuery;

    private Context mContext;

    SearchAdapter mAdapter;
    RecyclerView folderRecyclerView;
    SwipeRefreshLayout refreshLayout;

    //save and restore state
    private  SavedState mSavedState;

    public MaterialSearchView(Context context) {
        this(context, null);
    }

    public MaterialSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);

        mContext = context;

        initiateView();
    }


    private void initiateView() {
        LayoutInflater.from(mContext).inflate(R.layout.search_view, this, true);
        mSearchLayout = findViewById(R.id.searchLayout);

        mSearchTopBar = mSearchLayout.findViewById(R.id.search_top_bar);
        mSuggestionsRecyclerView =  mSearchLayout.findViewById(R.id.suggestionRecyclerView);
        mSearchSrcTextView = mSearchLayout.findViewById(R.id.searchTextView);
        mBackBtn = mSearchLayout.findViewById(R.id.backBtn);
        mVoiceBtn = mSearchLayout.findViewById(R.id.action_voice_btn);
        mEmptyBtn = mSearchLayout.findViewById(R.id.action_empty_btn);
        noResultFound = mSearchLayout.findViewById(R.id.nRF);
//        mTintView = mSearchLayout.findViewById(R.id.transparent_view);

        mSearchSrcTextView.setOnClickListener(mOnClickListener);
        mBackBtn.setOnClickListener(mOnClickListener);
        mVoiceBtn.setOnClickListener(mOnClickListener);
        mEmptyBtn.setOnClickListener(mOnClickListener);
//        mTintView.setOnClickListener(mOnClickListener);

        showVoice(true);

        mSuggestionsRecyclerView.setVisibility(GONE);
        noResultFound.setVisibility(GONE);
        setAnimationDuration(AnimationUtil.ANIMATION_DURATION_MEDIUM);

        buildRecyclerView();

        initiateSearchView();

    }

    private void buildRecyclerView() {
        mSuggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //Increase Scrolling Performance of recycler view
        mSuggestionsRecyclerView.setHasFixedSize(false);
        mSuggestionsRecyclerView.setItemViewCacheSize(20);
        mSuggestionsRecyclerView.setDrawingCacheEnabled(true);
        mSuggestionsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mSuggestionsRecyclerView.setNestedScrollingEnabled(false);

    }

    public void setFolderRecyclerView(RecyclerView recyclerView){
        folderRecyclerView = recyclerView;
    }
    public void setRefreshLayout(SwipeRefreshLayout refreshLayout){
        this.refreshLayout = refreshLayout;
    }

    public void setAdapter() {
        ArrayList<FilesInfo> filesList = Constant.allMemoryVideoList;
        mAdapter = new SearchAdapter(getContext(), filesList,refreshLayout,mSuggestionsRecyclerView,noResultFound,this);
        mSuggestionsRecyclerView.setAdapter(mAdapter);
    }

    private void initiateSearchView() {
        mSearchSrcTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    showKeyboard(mSearchSrcTextView);
            }
        });

        mSearchSrcTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mUserQuery = s;

                boolean hasText = !TextUtils.isEmpty(mUserQuery);
                if (hasText) {
                    mEmptyBtn.setVisibility(VISIBLE);
                    showVoice(false);
                    mAdapter.getFilter().filter(mUserQuery);

                } else {
                    mEmptyBtn.setVisibility(GONE);
                    showVoice(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private final OnClickListener mOnClickListener = new OnClickListener() {

        public void onClick(View v) {
            if (v == mBackBtn) {
                closeSearch();
            } else if (v == mVoiceBtn) {
                onVoiceClicked();
            } else if (v == mEmptyBtn) {
                mSearchSrcTextView.setText(null);
            } else if (v == mSearchSrcTextView) {
//                showSuggestions();
            }
//            else if (v == mTintView) {
////                closeSearch();
//                hideKeyboard(mTintView);
//            }
        }
    };


    private void onVoiceClicked() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak an item name or number");    // user hint
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);    // setting recognition model, optimized for short phrases – search queries
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);    // quantity of results we want to receive
        if (mContext instanceof Activity) {
            ((Activity) mContext).startActivityForResult(intent, REQUEST_VOICE);
        }
    }

    private boolean isVoiceAvailable() {
        if (isInEditMode()) {
            return true;
        }
        PackageManager pm = getContext().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return activities.size() == 0;
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showKeyboard(View view) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1 && view.hasFocus()) {
            view.clearFocus();
        }
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
    }


    /**
     * if show is true, this will enable voice search. If voice is not available on the device, this method call has not effect.
     *
     * @param show
     */
    public void showVoice(boolean show) {
        if (show && isVoiceAvailable()) {
            mVoiceBtn.setVisibility(VISIBLE);
        } else {
            mVoiceBtn.setVisibility(GONE);
        }
    }

    /**
     * Call this method and pass the menu item so this class can handle click events for the Menu Item.
     *
     * @param menuItem
     */
    public void setMenuItem(MenuItem menuItem) {
        this.mMenuItem = menuItem;
        setAdapter();
        mMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showSearch();
                return true;
            }
        });
    }

    /**
     * Return true if search is open
     *
     * @return
     */
    public boolean isSearchOpen() {
        return mIsSearchOpen;
    }

    public void dismissSuggestions() {
        if (mSuggestionsRecyclerView.getVisibility() == View.VISIBLE) {
            mSuggestionsRecyclerView.setVisibility(View.GONE);
        }
    }

    /**
     * Sets animation duration. ONLY FOR PRE-LOLLIPOP!!
     *
     * @param duration duration of the animation
     */
    public void setAnimationDuration(int duration) {
        mAnimationDuration = duration;
    }

    /**
     * Open Search View. This will animate the showing of the view.
     */
    public void showSearch() {
        showSearch(true);
    }

    /**
     * Open Search View. If animate is true, Animate the showing of the view.
     *
     * @param animate true for animate
     */
    public void showSearch(boolean animate) {
        if (isSearchOpen()) {
            return;
        }

        //Request Focus
        mSearchSrcTextView.setText(null);
        mSearchSrcTextView.requestFocus();

        if (animate) {
            setVisibleWithAnimation();

        } else {
            mSearchLayout.setVisibility(VISIBLE);
        }
        mIsSearchOpen = true;
    }

    private void setVisibleWithAnimation() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSearchLayout.setVisibility(View.VISIBLE);
            AnimationUtil.reveal(mSearchTopBar);

        } else {
            AnimationUtil.fadeInView(mSearchLayout, mAnimationDuration);
        }
    }

    /**
     * Close search view.
     */
    public void closeSearch() {
        if (!isSearchOpen()) {
            return;
        }

        mSearchSrcTextView.setText(null);
        dismissSuggestions();
        clearFocus();

        mSearchLayout.setVisibility(GONE);
        refreshLayout.setVisibility(VISIBLE);

        mIsSearchOpen = false;

    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        // Don't accept focus if in the middle of clearing focus
        if (mClearingFocus) return false;
        // Check if SearchView is focusable.
        if (!isFocusable()) return false;
        return mSearchSrcTextView.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    public void clearFocus() {
        mClearingFocus = true;
        hideKeyboard(this);
        super.clearFocus();
        mSearchSrcTextView.clearFocus();
        mClearingFocus = false;
    }



    //save and restore part
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        mSavedState = new SavedState(superState);
        mSavedState.query = mUserQuery != null ? mUserQuery.toString() : null;
        mSavedState.isSearchOpen = this.mIsSearchOpen;

        return mSavedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

//        Toast.makeText(getContext(),"restore state",Toast.LENGTH_SHORT).show();

        mSavedState = (SavedState) state;

        if (mSavedState.isSearchOpen) {
            showSearch(false);
            setQuery(mSavedState.query, false);
        }

        super.onRestoreInstanceState(mSavedState.getSuperState());
    }

    static class SavedState extends BaseSavedState {
        String query;
        boolean isSearchOpen;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.query = in.readString();
            this.isSearchOpen = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(query);
            out.writeInt(isSearchOpen ? 1 : 0);
        }

        //required field that makes Parcelables from a Parcel
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    /**
     * Calling this will set the query to search text box. if submit is true, it'll submit the query.
     *
     * @param query
     * @param submit
     */
    public void setQuery(CharSequence query, boolean submit) {
        mSearchSrcTextView.setText(query);
        if (query != null) {
            mSearchSrcTextView.setSelection(mSearchSrcTextView.length());
            mUserQuery = query;
        }
        if (submit && !TextUtils.isEmpty(query)) {
            onSubmitQuery();
        }
    }

    private void onSubmitQuery() {
        CharSequence query = mSearchSrcTextView.getText();
//        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
//            if (mOnQueryChangeListener == null || !mOnQueryChangeListener.onQueryTextSubmit(query.toString())) {
//                closeSearch();
//                mSearchSrcTextView.setText(null);
//            }
//        }
    }
    //OpenFolderListener Start ------>

    @Override
    public void onVideoClick(int position) {
        Intent intent = new Intent(mContext, VideoPlayerActivity.class);
        intent.putExtra("videoPosition",position);
        intent.putExtra("DATA_TYPE",1);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mContext.startActivity(intent);
    }

    @Override
    public void onIconMoreClick(int videoPosition) {
//        setVideoPosition(videoPosition);
//        //Open Bottom Modal Sheet
//        VideoOptionBottomSheetDialog bottomSheetDialog = new VideoOptionBottomSheetDialog();
//        bottomSheetDialog.setPosition(videoPosition);
//        bottomSheetDialog.show(getSupportFragmentManager(), bottomSheetDialog.getTag());
    }

    @Override
    public boolean onVideoLongClick(int position) {
        //Do nothing
        return true;
    }

    //OpenFolderListener End ------>

    public void updateAdapter(){
        mAdapter.notifyDataSetChanged();
    }
}