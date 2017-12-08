package software.sic.droid.google_dni.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebViewClient;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import software.sic.droid.google_dni.data.event.NewsEvent;
import software.sic.droid.google_dni.data.event.UiEvent;
import software.sic.droid.google_dni.ui.activities.WebViewActivity;

import static software.sic.droid.google_dni.BuildConfig.DEBUG;

/**
 * www.sic.software
 *
 * 20.04.17
 */

public class VideoEnabledWebViewWithLog extends VideoEnabledWebView {

    public static final String INJECTED_JS_OBJECT_ID = "injectedObject";

    public VideoEnabledWebViewWithLog(Context context) {
        super(context);
        this.setWebViewClient(new WebViewClient());
    }

    public VideoEnabledWebViewWithLog(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setWebViewClient(new WebViewClient());
    }

    public VideoEnabledWebViewWithLog(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setWebViewClient(new WebViewClient());
    }

    public void load(@NonNull WebViewActivity webViewActivityContext, @NonNull NewsEvent newsEvent, @NonNull String baseUrl) {

        if(newsEvent.type == NewsEvent.Type.VIDEO) {
            loadVideoNews(webViewActivityContext, newsEvent, baseUrl);
        } else {
            this.loadDataWithBaseURL(null, newsEvent.content, "text/html", null, null);
        }
    }

    public void loadVideoNews(@NonNull WebViewActivity webViewActivityContext, @NonNull NewsEvent newsEvent,
                              @NonNull String baseUrl) {
        VideoStatsJavascriptInterface mStats = new VideoStatsJavascriptInterface(webViewActivityContext);

        String originHtml = newsEvent.content;
        originHtml = removeVideoDivs(originHtml);

        int numberOfVideos = 0;
        int oldIndex = 0;
        int currentIndex = 0;

        while (true) {
            currentIndex = originHtml.indexOf("</video>", currentIndex);
            if(currentIndex == -1) {
                break;
            }
            currentIndex++;
            numberOfVideos++;
        }

        addJavascriptInterface(mStats, INJECTED_JS_OBJECT_ID);
        loadDataWithBaseURL(baseUrl, getVideoWebViewHtml(originHtml), "text/html", null, null);
    }

    //List of all video-element events: https://www.w3.org/TR/html5/embedded-content-0.html#mediaevents
    private String getVideoWebViewHtml(String content) {
        return getHead(content) + content + getTail();
    }//getVideoWebViewHtml

    private String removeVideoDivs(String content) {
        int currentIndex = 0;
        String result = "";

        while(currentIndex < content.length()) {
            int divHeadStart = content.indexOf("<div", currentIndex);
            int divHeadEnd = content.indexOf(">", divHeadStart);

            int divTailStart = content.indexOf("</div>", divHeadEnd);
            int divTailEnd = divTailStart + 5;

            //if no div could be found, return current state
            if( (divHeadStart < 0) || (divHeadEnd < 0) || (divTailStart < 0) ) {
                return result += content.substring(currentIndex);
            }

            result += content.substring(currentIndex, divHeadStart)
                    + content.substring(divHeadEnd + 1, divTailStart);

            currentIndex = ++divTailEnd;
        }

        return result;
    }//removeVideoDivs

    private String getHead(String content) {
        String headA = " <!DOCTYPE html> <head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=UTF-8\"> ";
        String headB = "</head><body> ";

        String jsA = " <script> function init() { ";
        String jsB = " } document.addEventListener(\"DOMContentLoaded\", init, false); function " +
                "addAllEventListeners(element) { element.addEventListener(\"ended\", capture, false); element" +
                ".addEventListener(\"play\", capture, false); } function capture(event) { injectedObject" +
                ".incrementStat(event.type); } </script> ";

        String css = " <style> body {  color: #000000; font-family: Arial, sans-serif; }" +
                " video { padding: 0; margin: 0; width: 100%; height: auto; margin: auto; float: left; object-fit: " +
                "contain;} video::-internal-media-controls-download-button { display:none; } " +
                "video::-webkit-media-controls-enclosure { overflow:hidden; } video::-webkit-media-controls-panel { " +
                "width: calc(100% + 35px); } </style> ";

        List<String> videoId = getVideoIds(content);
        if(videoId.isEmpty()) {
            return  headA + headB;
        }

        return headA + jsA + getJSEventListenerString(videoId) + jsB + css + headB;
    }//getHead

    @NonNull
    private List<String> getVideoIds(String content) {
        List<String> ids = new Vector<>();
        int currentIndex = 0;

        while(currentIndex < content.length()) {
            int videoTagStart = content.indexOf("<video", currentIndex);
            int videoTagEnd = content.indexOf("</video>", videoTagStart);
            currentIndex = ++videoTagEnd;

            //if no video could be found, return vector
            if( (videoTagStart < 0) || (videoTagEnd < 0) ) {
                return ids;
            }

            int idStart = (content.indexOf("id=", videoTagStart) + 4);
            int idEnd = content.indexOf("\"", idStart + 4);

            //if video has id, isolate it
            if((idStart >= 0) && (idEnd >= 0)) {
                ids.add(content.substring(idStart, idEnd));
            }
        }

        return ids;
    }//getVideoId

    private String getJSEventListenerString(@ NonNull List<String> ids) {
        int varIndex = 0;
        String variables = "";
        String eventListeners = "";
        Iterator<String> iterator = ids.iterator();

        while(iterator.hasNext()) {
            String id = iterator.next();
            variables += " document._video" + varIndex + " = document.getElementById( \"" + id + "\" ); ";
            eventListeners += " addAllEventListeners( document._video" + varIndex + " ); ";
            varIndex++;
        }
        return variables + eventListeners;
    }//getJSEventListenerString

    private String getTail() {
        return " </body></html> ";
    }//getTail

    private class VideoStatsJavascriptInterface extends JavascriptInterface {

        private WebViewActivity mContext = null;
        private int endedStat = 0;
        private int playStat = 0;

        public VideoStatsJavascriptInterface(WebViewActivity context) {
            mContext = context;
        }

        @android.webkit.JavascriptInterface
        public void incrementStat(String stat) {
            switch (stat) {
                case "ended":
                    if(DEBUG){
                        Log.d("webView", "VideoEvent: ended");}
                    mContext.updateNewsEventInDatabase(UiEvent.Action.VIDEO_FINISHED);
                    endedStat++;
                    break;
                case "play":
                    if(DEBUG){Log.d("webView", "VideoEvent: play");}
                    mContext.updateNewsEventInDatabase(UiEvent.Action.VIDEO_PLAY);
                    playStat++;
                    break;
                default:
                    break;
            }
        }

        public int getEndedStat() {
            return endedStat;
        }

        public int getPlayStat() {
            return playStat;
        }

    }

}
