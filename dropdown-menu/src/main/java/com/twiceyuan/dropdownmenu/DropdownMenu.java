package com.twiceyuan.dropdownmenu;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.twiceyuan.dropdownmenu.widget.FontIcon;

/**
 * Created by twiceYuan on 10/21/15.
 * <p/>
 * 下拉菜单，通过 PopupWindow 实现
 */
public class DropdownMenu extends RelativeLayout {

    @SuppressWarnings("FieldCanBeLocal")
    private Context                     mContext;
    private PopupWindow                 mPopupWindow;
    private FixedHeightListView         mListView;
    @SuppressWarnings("FieldCanBeLocal")
    private RelativeLayout              mShadowLayout;
    private OnDropdownItemClickListener mItemClickListener;
    private TextView                    textView;
    private FontIcon                    iconView;
    private DropdownAdapter             mDropdownAdapter;
    private OnClickListener             mSecondClickListener;

    private static final String ICON_LESS = "\ue5ce";
    private static final String ICON_MORE = "\ue5cf";

    private static final int NO_HIGHLIGHT = -1; // 没有设置高亮色时的默认值

    public DropdownMenu(Context context) {
        super(context);
        init(context, null);
    }

    public DropdownMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DropdownMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DropdownMenu(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        // 初始化属性
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.DropdownMenu);
        String titleText = attributes.getString(
                R.styleable.DropdownMenu_titleText);
        float textSize = attributes.getDimensionPixelSize(
                R.styleable.DropdownMenu_titleTextSize, 0);
        final int textColor = attributes.getColor(
                R.styleable.DropdownMenu_titleColor,
                0xff000000);
        int titleBgColor = attributes.getColor(
                R.styleable.DropdownMenu_titleBgColor,
                0xffcccccc);
        int listBgColor = attributes.getColor(
                R.styleable.DropdownMenu_listBgColor,
                0x00ffffff);
        final int iconColor = attributes.getColor(
                R.styleable.DropdownMenu_iconColor,
                0xffcccccc);
        final int highLightColor = attributes.getColor(
                R.styleable.DropdownMenu_highlightColor,
                NO_HIGHLIGHT);

        iconView = new FontIcon(mContext);
        iconView.setTextColor(iconColor);
        iconView.setGravity(Gravity.CENTER);
        iconView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        attributes.recycle();

        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setBackgroundColor(titleBgColor);
        setGravity(Gravity.CENTER);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupWindow = inflater.inflate(R.layout.ddm_popup_list, (ViewGroup) getParent(), false);

        mPopupWindow = new PopupWindow(popupWindow, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);

        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);

        // 不加这个在低版本（测试了 4.1）上会有外部点击事件不会响应的问题
        //noinspection deprecation
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

        mListView = (FixedHeightListView) popupWindow.findViewById(R.id.lv_menu);
        mListView.setBackgroundColor(listBgColor);
        mListView.setAdapter(mDropdownAdapter = new ArrayDropdownAdapter(
                mContext,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"no data"}));
        mShadowLayout = (RelativeLayout) popupWindow.findViewById(R.id.rl_menu_shadow);
        mShadowLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(parent, view, position, id);
                }
                textView.setText(mDropdownAdapter.getTitleString(position));
                mPopupWindow.dismiss();
            }
        });

        textView = new TextView(mContext);

        LayoutParams titleParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.addRule(CENTER_IN_PARENT, TRUE);
        textView.setLayoutParams(titleParams);

        textView.setText(TextUtils.isEmpty(titleText) ? "[未配置]" : titleText);
        textView.setTextColor(textColor);
        textView.setBackgroundColor(titleBgColor);
        textView.setPadding(20, 0, 32, 0);
        textView.setGravity(Gravity.CENTER);
        if (textSize > 0) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }

        LayoutParams iconParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        iconParams.addRule(ALIGN_PARENT_RIGHT, TRUE);
        iconParams.addRule(CENTER_VERTICAL, TRUE);
        iconView.setLayoutParams(iconParams);

        iconView.setPadding(20, 8, 32, 0);
        iconView.setText(ICON_LESS);

        addView(textView);
        addView(iconView);

        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                iconView.setText(ICON_LESS);
                iconView.setTextColor(iconColor);
                textView.setTextColor(textColor);
            }
        });

        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                    iconView.setText(ICON_LESS);
                    iconView.setTextColor(iconColor);
                    textView.setTextColor(textColor);
                } else {
                    mPopupWindow.showAsDropDown(DropdownMenu.this);
                    mPopupWindow.setOutsideTouchable(true);
                    iconView.setText(ICON_MORE);

                    if (highLightColor != -1) {
                        iconView.setTextColor(highLightColor);
                        textView.setTextColor(highLightColor);
                    }
                }
                if (mSecondClickListener != null) {
                    mSecondClickListener.onClick(DropdownMenu.this);
                }
            }
        });
    }

    public void setAdapter(DropdownAdapter adapter) {
        mListView.setAdapter(mDropdownAdapter = adapter);
    }

    public void setOnItemClickListener(final OnDropdownItemClickListener listener) {
        mItemClickListener = listener;
    }

    /**
     * 配置自定义 View，比如多级联动或者网格选择的，将最终返回结果的 AbsListView 传进来并设置回调监听
     *
     * @param customView 自定义内容区域
     * @param listener   选中监听器
     */
    public void setCustomView(
            final ViewGroup contentView,
            final AbsListView customView,
            final OnDropdownItemClickListener listener) {

        LinearLayout container = (LinearLayout) mPopupWindow.getContentView().findViewById(R.id.container);

        container.removeAllViews();
        container.addView(contentView);

        customView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onItemClick(parent, view, position, id);
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    Adapter adapter = parent.getAdapter();
                    if (adapter instanceof ArrayDropdownAdapter) {
                        setTitle(((ArrayDropdownAdapter) adapter).getTitleString(position));
                    }
                    mPopupWindow.dismiss();
                }
            }
        });
    }

    /**
     * 设置默认标题文字
     *
     * @param title 内容
     */
    @SuppressWarnings("unused") public void setTitle(String title) {
        textView.setText(title);
    }

    /**
     * 下拉菜单是否在显示
     */
    @SuppressWarnings("unused") public boolean isDropdown() {
        return mPopupWindow != null && mPopupWindow.isShowing();
    }

    /**
     * 如果存在，展开
     */
    @SuppressWarnings("unused") public void expand() {
        if (mPopupWindow != null) {
            mPopupWindow.showAsDropDown(this);
        }
    }

    /**
     * 如果存在，收起
     */
    public void collapse() {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
    }

    public void addOnClickListener(OnClickListener l) {
        mSecondClickListener = l;
    }

    public FixedHeightListView getListView() {
        return mListView;
    }
}
