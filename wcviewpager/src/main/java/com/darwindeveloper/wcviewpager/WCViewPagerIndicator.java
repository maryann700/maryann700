package com.darwindeveloper.wcviewpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by Darwin Morocho on 11/3/2017.
 */

public class WCViewPagerIndicator extends LinearLayout {
    private ViewPager viewPager;
    private Context context;
    private int numPages = 0;// numero de paginas en el pager
    private ArrayList<Indicator> indicators = new ArrayList<>();
    private IndicadorAdapter indicadorAdapter;
    private RecyclerView recyclerViewIndicators;


    private boolean showNumbers;
    private int indicatorsColor, indicatorSelectedColor, numbersColor, numberSeletedColor;

    public WCViewPagerIndicator(Context context) {
        super(context);
        this.context = context;
    }

    public WCViewPagerIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;


        //get the attributes specified in attrs.xml using the name we included
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WCViewPagerIndicator, 0, 0);
        try {
            //get the text and colors specified using the names in attrs.xml
            indicatorsColor = a.getColor(R.styleable.WCViewPagerIndicator_indicatorsColor, Color.parseColor("#0099cc"));
            indicatorSelectedColor = a.getColor(R.styleable.WCViewPagerIndicator_indicatorSelectedColor, Color.parseColor("#FF093896"));
            numbersColor = a.getColor(R.styleable.WCViewPagerIndicator_numbersColor, Color.parseColor("#ffffff"));
            numberSeletedColor = a.getColor(R.styleable.WCViewPagerIndicator_numberSelectedColor, Color.parseColor("#ffffff"));
            showNumbers = a.getBoolean(R.styleable.WCViewPagerIndicator_showNumbers, true);

        } finally {
            a.recycle();
        }
        init();
    }


    private void init() {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.wcviewpager, this, true);

        recyclerViewIndicators = (RecyclerView) findViewById(R.id.recyclerviewIndicators);
        recyclerViewIndicators.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
    }


    /**
     * aplica el adapter a nuestro viewpager
     */
    public void setViewPager(final ViewPager viewPager) {
        this.viewPager = viewPager;
        numPages = viewPager.getAdapter().getCount();

        indicators.add(new Indicator("1", true));
        for (int i = 1; i < numPages; i++) {
            indicators.add(new Indicator("" + (i + 1)));
        }

        indicadorAdapter = new IndicadorAdapter(context, indicators, indicatorsColor, indicatorSelectedColor, numbersColor, showNumbers, numberSeletedColor);
        indicadorAdapter.setOnIndicatorClickListener(new IndicadorAdapter.OnIndicatorClickListener() {
            @Override
            public void onIndicatorClick(int position) {
                viewPager.setCurrentItem(position);
                setSelectedindicator(position);
            }
        });
        recyclerViewIndicators.setAdapter(indicadorAdapter);
    }

    /**
     * @return numero de paginas del pager
     */
    public int getNumPages() {
        return numPages;
    }

    /**
     * @return retorna nuestro view pager
     */
    public ViewPager getViewPager() {
        return viewPager;
    }


    /**
     * cambia la posicion del indicador seleccionado
     *
     * @param position
     */
    public void setSelectedindicator(int position) {
        if (numPages > 0) {
            for (int i = 0; i < indicators.size(); i++) {
                if (i == position) {
                    indicators.get(i).setSelected(true);
                } else {
                    indicators.get(i).setSelected(false);
                }
            }
            indicadorAdapter.notifyItemChanged(position);
            indicadorAdapter.notifyDataSetChanged();
        }

    }

}
