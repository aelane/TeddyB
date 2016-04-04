package AWS_Classes.Dynamo.Metrics;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;

import AWS_Classes.Dynamo.Settings.BearData;

/**
 * Created by Niko on 4/3/2016.
 */
public interface MetricsResponse {
    void metricsFinish(PaginatedQueryList<Metrics> output);
}
