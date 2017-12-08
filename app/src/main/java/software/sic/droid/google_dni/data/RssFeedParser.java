/*
 * www.sic.software
 *
 * @file RssFeedParser.java
 * @date 2017-01-25
 * @brief parse XML data from RssFeed
 */
package software.sic.droid.google_dni.data;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import software.sic.droid.google_dni.data.event.NewsEvent;
import software.sic.droid.google_dni.util.SmartNewsSharedPreferences;

class RssFeedParser {
    private static final String RSS_FEED_URL = "http://gdni.sic.software.w0123d63.kasserver.com/feed/";
    private static final String RSS_FEED_NAMESPACE = "";
    private static final String RSS_FEED_OUTER_SECTION_TAG = "rss";
    private static final String RSS_FEED_INNER_SECTION_TAG = "channel";
    private static final String RSS_FEED_ENTRY_SECTION_TAG = "item";
    private static final String RSS_FEED_ENTRY_TAG_TITLE = "title";
    private static final String RSS_FEED_ENTRY_TAG_SUMMARY = "description";
    private static final String RSS_FEED_ENTRY_TAG_CONTENT = "content:encoded";
    private static final String RSS_FEED_ENTRY_TAG_ID = "guid";
    private static final String RSS_FEED_ENTRY_TAG_TYPE = "category";
    private static final String RSS_FEED_ENTRY_TAG_TIMESTAMP = "pubDate";

    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);

    private InputStream mIn = null;
    private XmlPullParser mParser = null;
    private HttpURLConnection urlConnection = null;
    private SmartNewsSharedPreferences mSharedPreferences = null;

    RssFeedParser() {

        mSharedPreferences = SmartNewsSharedPreferences.instance();

        try {
            mParser = Xml.newPullParser();
            mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }//RssFeedParser

    private boolean openConnection() throws XmlPullParserException, IOException {
        String eTag = mSharedPreferences.getCurrentStoredEtag();
        urlConnection = (HttpURLConnection) (new URL(RSS_FEED_URL)).openConnection();

        if (eTag != null) {

            urlConnection.setRequestProperty("IF-NONE-MATCH", eTag);

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                return false;

            } else {
                String currETag = urlConnection.getHeaderField("etag");

                mIn = new BufferedInputStream(urlConnection.getInputStream());
                mParser.setInput(mIn, null);

                mSharedPreferences.saveLastETag(currETag);

                return true;
            }

        } else {
            String currETag = urlConnection.getHeaderField("etag");
            mIn = new BufferedInputStream(urlConnection.getInputStream());
            mParser.setInput(mIn, null);

            mSharedPreferences.saveLastETag(currETag);
            return true;
        }
    }

    List<NewsEvent> parseFeed() throws XmlPullParserException, IOException {
        List<NewsEvent> entries = null;
        String name;

        if (openConnection()) {
            entries = new ArrayList<>();

            mParser.nextTag();
            mParser.require(XmlPullParser.START_TAG, RSS_FEED_NAMESPACE, RSS_FEED_OUTER_SECTION_TAG);

            while (mParser.next() != XmlPullParser.END_TAG) {

                if (mParser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                name = mParser.getName();
                if (name.equals(RSS_FEED_INNER_SECTION_TAG)) {

                    while (mParser.next() != XmlPullParser.END_TAG) {

                        if (mParser.getEventType() != XmlPullParser.START_TAG) {
                            continue;
                        }

                        name = mParser.getName();
                        if (name.equals(RSS_FEED_ENTRY_SECTION_TAG)) {
                            entries.add(readEntry());
                        } else {
                            skipTag();
                        }
                    }
                } else {
                    skipTag();
                }
            }

        }
        close();

        return entries;
    }//parseFeed

    private NewsEvent readEntry() throws XmlPullParserException, IOException {
        mParser.require(XmlPullParser.START_TAG, RSS_FEED_NAMESPACE, RSS_FEED_ENTRY_SECTION_TAG);
        Date time = null;
        String title = null;
        String summary = null;
        String content = null;
        String id = null;
        String timestampString = null;
        int type = -1;

        while (mParser.next() != XmlPullParser.END_TAG) {

            if (mParser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            switch (mParser.getName()) {
                case RSS_FEED_ENTRY_TAG_TITLE:
                    title = readTitle();
                    break;
                case RSS_FEED_ENTRY_TAG_SUMMARY:
                    summary = readSummary();
                    break;
                case RSS_FEED_ENTRY_TAG_CONTENT:
                    content = readContent();
                    break;
                case RSS_FEED_ENTRY_TAG_ID:
                    id = readId();
                    break;
                case RSS_FEED_ENTRY_TAG_TYPE:
                    type = readType();
                    break;
                case RSS_FEED_ENTRY_TAG_TIMESTAMP:
                    timestampString = readTimestamp();
                    break;
                default:
                    skipTag();
                    break;
            }
        }

        try {
            time = this.mSimpleDateFormat.parse(timestampString);
        } catch (ParseException e) {
            time = new Date(0);
        }

        return new NewsEvent(0, time.getTime(), id, title, summary, content, type);
    }//readEntry

    private String readTitle() throws IOException, XmlPullParserException {
        mParser.require(XmlPullParser.START_TAG, RSS_FEED_NAMESPACE, RSS_FEED_ENTRY_TAG_TITLE);

        String title = "";

        if (mParser.next() == XmlPullParser.TEXT) {
            title = mParser.getText();
            mParser.nextTag();
        }

        mParser.require(XmlPullParser.END_TAG, RSS_FEED_NAMESPACE, RSS_FEED_ENTRY_TAG_TITLE);
        return title;
    }//readTitle

    private String readSummary() throws IOException, XmlPullParserException {
        mParser.require(XmlPullParser.START_TAG, RSS_FEED_NAMESPACE, RSS_FEED_ENTRY_TAG_SUMMARY);

        String summary = "";

        if (mParser.next() == XmlPullParser.TEXT) {
            summary = mParser.getText();
            mParser.nextTag();
        }

        mParser.require(XmlPullParser.END_TAG, RSS_FEED_NAMESPACE, RSS_FEED_ENTRY_TAG_SUMMARY);
        return summary;
    }//readSummary

    private String readContent() throws IOException, XmlPullParserException {
        mParser.require(XmlPullParser.START_TAG, RSS_FEED_NAMESPACE, RSS_FEED_ENTRY_TAG_CONTENT);

        String content = "";

        if (mParser.next() == XmlPullParser.TEXT) {
            content = mParser.getText();
            mParser.nextTag();
        }

        mParser.require(XmlPullParser.END_TAG, RSS_FEED_NAMESPACE, RSS_FEED_ENTRY_TAG_CONTENT);
        return content;
    }//readContent

    private String readId() throws IOException, XmlPullParserException {
        mParser.require(XmlPullParser.START_TAG, RSS_FEED_NAMESPACE, RSS_FEED_ENTRY_TAG_ID);

        String content = "";

        if (mParser.next() == XmlPullParser.TEXT) {
            content = mParser.getText();
            mParser.nextTag();
        }

        content = content.substring(content.lastIndexOf("?p=") + 3);

        mParser.require(XmlPullParser.END_TAG, RSS_FEED_NAMESPACE, RSS_FEED_ENTRY_TAG_ID);

        return content;
    }//readId

    private int readType() throws IOException, XmlPullParserException {
        mParser.require(XmlPullParser.START_TAG, RSS_FEED_NAMESPACE, RSS_FEED_ENTRY_TAG_TYPE);

        String type = "";

        if (mParser.next() == XmlPullParser.TEXT) {
            type = mParser.getText();
            mParser.nextTag();
        }

        mParser.require(XmlPullParser.END_TAG, RSS_FEED_NAMESPACE, RSS_FEED_ENTRY_TAG_TYPE);

        int intType;
        try {
            intType = NewsEvent.Type.valueOf(type).ordinal();
        } catch (IllegalArgumentException e) {
            intType = -1;
        }
        return intType;
    }//readType

    private String readTimestamp() throws IOException, XmlPullParserException {
        mParser.require(XmlPullParser.START_TAG, RSS_FEED_NAMESPACE, RSS_FEED_ENTRY_TAG_TIMESTAMP);

        String content = "";

        if (mParser.next() == XmlPullParser.TEXT) {
            content = mParser.getText();
            mParser.nextTag();
        }

        mParser.require(XmlPullParser.END_TAG, RSS_FEED_NAMESPACE, RSS_FEED_ENTRY_TAG_TIMESTAMP);

        return content;
    }//readId

    private void skipTag() throws XmlPullParserException, IOException {
        if (mParser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }

        int depth = 1;

        while (depth != 0) {
            switch (mParser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;

                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }//skipTag

    private void close() throws IOException {
        if (mIn != null) {
            mIn.close();
        }
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }//close

}//class RssFeedParser
//#############################################################################
//eof RssFeedParser.java
