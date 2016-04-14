package Helper_Classes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import org.achartengine.ChartFactory;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import java.util.List;

/**
 * Created by pwdarby on 4/13/16.
 */
public class makePieChart {

    public Intent execute(Context context, String title, List<String> known, List<String> trouble) {
        int[] colors = new int[] { Color.RED, Color.YELLOW, Color.BLUE };
        DefaultRenderer renderer = buildCategoryRenderer(colors, title);

        CategorySeries categorySeries = new CategorySeries("Words");
        categorySeries.add("known words", known.size());
        categorySeries.add("trouble words", trouble.size());
        categorySeries.add("words not covered", (51 - known.size() - trouble.size()));



        return ChartFactory.getPieChartIntent(context, categorySeries, renderer, null);
    }

    protected DefaultRenderer buildCategoryRenderer(int[] colors, String title) {
        DefaultRenderer renderer = new DefaultRenderer();

        renderer.setLabelsColor(Color.BLACK);
        renderer.setChartTitle(title);
        renderer.setChartTitleTextSize(80);
        renderer.setLegendTextSize(40);

        for (int color : colors) {
            SimpleSeriesRenderer r = new SimpleSeriesRenderer();
            r.setColor(color);
            renderer.addSeriesRenderer(r);
        }
        return renderer;
    }
}