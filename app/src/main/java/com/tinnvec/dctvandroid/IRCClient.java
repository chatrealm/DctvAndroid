package com.tinnvec.dctvandroid;

import android.graphics.Color;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import org.jibble.pircbot.PircBot;

import java.util.ArrayList;


public class IRCClient extends PircBot implements Runnable {

    static final String MESSAGE = "new_message";
    static final String NICK = "new_nick";
    static final String TYPE = "new_type";
    private static IRCClient instance;
    private static String nick = "nick";
    public ArrayList<SpannableString> chatlinesBackground;
    private Thread thread;
    private String newMessage;
    private String newNick;
    private MessageType newType = MessageType.DEFAULT;
    private SpannableStringBuilder line;


    public IRCClient() {

        this.setName(nick);
        this.setLogin("AndroidDCTVApp");
        this.setVersion("DCTV Android App - version " + BuildConfig.VERSION_NAME);
        chatlinesBackground = new ArrayList<>();
        thread = new Thread(this);
        thread.start();
    }

    public static IRCClient getInstance() {
        if (instance == null) {
            instance = new IRCClient();
        }
        return instance;
    }

    public static void setNick(String n) {
        nick = n;
        instance.setName(nick);
    }

    public static boolean isRunningAndConnected() {
        if (instance == null) {
            return false;
        } else return instance.isConnected();
    }

    public void onConnect() {
        newType = MessageType.ON_CONNECT;
        newNick = "";
        newMessage = "Logged in to " + getServer();
        run();
    }

    public void onJoin(String channel, String sender, String login, String hostname) {
        newType = MessageType.ON_JOIN_PART;
        if (sender.equals(getNick())) {
            newNick = "";
            newMessage = "Succesfully joined channel " + channel;
        } else {
            newNick = "";
            newMessage = sender + " joined the channel";
        }
        run();
    }

    public void onMessage(String channel, String sender,
                          String login, String hostname, String message) {
        newType = MessageType.DEFAULT;
        newMessage = message;
        newNick = sender;
        run();
    }

    public void onAction(String sender, String login, String hostname, String target, String action) {
        newType = MessageType.ACTION;
        newMessage = action;
        newNick = sender;
        run();
    }

    public void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
        newType = MessageType.ON_TOPIC;
        if (changed) {
            newMessage = "New topic: " + topic;
            newNick = "";
        } else {
            newMessage = "Topic: " + topic;
            newNick = "";
        }
        run();
    }

    public void onPart(String channel, String sender, String login, String hostname) {
        newType = MessageType.ON_JOIN_PART;
        newNick = "";
        newMessage = sender + " left the channel";
        run();
    }

    @Override
    public void run() {
        addChatline(newType, newNick, newMessage);
        Message msg = NativeChatFragment.handler.obtainMessage(NativeChatFragment.NEW_CHATLINE);
        NativeChatFragment.handler.sendMessage(msg);
    }

    public void addChatline(MessageType type, String nick, String message) {
        if (message != null && nick != null) {
            int color = 0xff1565c0;
            ForegroundColorSpan fcs = new ForegroundColorSpan(color);
            SpannableStringBuilder line = new SpannableStringBuilder();
            switch (type) {
                case DEFAULT:
                    line.append("<" + nick + "> ");
                    line.setSpan(fcs, 0, line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    line.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    line.append(message);
                    break;
                case ACTION:
                    line.append(nick + " ");
                    line.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    line.append(message);
                    line.setSpan(fcs, 0, line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case ON_ERROR:
                    int errorcolor = Color.RED;
                    ForegroundColorSpan errorfcs = new ForegroundColorSpan(errorcolor);
                    line.append(nick + " ");
                    line.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    line.append(message);
                    line.setSpan(errorfcs, 0, line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case ON_TOPIC:
                case ON_CONNECT:
                case ON_JOIN_PART:
                    line.append(message);
            }

            chatlinesBackground.add(SpannableString.valueOf(line));
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        instance = null;

        Thread.currentThread().interrupt();
    }

    public enum MessageType {
        DEFAULT("default"),
        ACTION("action"),
        ON_CONNECT("on_connect"),
        ON_JOIN_PART("on_join_part"),
        ON_TOPIC("on_topic"),
        ON_ERROR("on_error");

        private String stringValue = "";

        MessageType(String toString) {
            stringValue = toString;
        }

        @Override
        public String toString() {
            return stringValue;
        }
    }

}
