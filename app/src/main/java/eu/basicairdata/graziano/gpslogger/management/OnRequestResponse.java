package eu.basicairdata.graziano.gpslogger.management;

/**
 * @see RequestTrackManager
 * @see RequestRecordManager
 * @param <V> Response arg Type
 */
public interface OnRequestResponse<V> {
    void onRequestResponse(final V response, final boolean isSuccess);
}