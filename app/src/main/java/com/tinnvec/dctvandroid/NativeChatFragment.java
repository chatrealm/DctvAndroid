package com.tinnvec.dctvandroid;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

import java.io.IOException;
import java.util.ArrayList;

public class NativeChatFragment extends Fragment {
    public static final int NEW_CHATLINE = 0;
    public static ListView chatListView;
    public static ArrayList<SpannableString> chatlines;
    public static Handler handler;
    public static Handler UIHandler = new Handler(Looper.getMainLooper());
    static ArrayAdapter<SpannableString> chatAdapter;
    private static IRCClient ircClient;
    private static View rootview;
    private static View snackbaranchor;

    static {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case NativeChatFragment.NEW_CHATLINE:
                        NativeChatFragment.updateChatline();
                        break;
                }
            }
        };
    }

    NativeChatFragment chatActivity;
    private String chatInput;
    private EditText chatInputEditText;
    private String channel;
    private String nick;
    private String server;

    public static void updateChatline() {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (ircClient.chatlinesBackground.size() > 0) {
                    if (ircClient.chatlinesBackground.size() != chatlines.size()) {
                        int l = ircClient.chatlinesBackground.size() - 1;
                        SpannableString newLine = ircClient.chatlinesBackground.get(l);
                        chatlines.add(newLine);
                    }
                }


                final int position = chatlines.size() - 1;

                chatAdapter.notifyDataSetChanged();

                if (chatListView.getLastVisiblePosition() > (position - 5)) {
                    chatListView.smoothScrollToPosition(position);
                } else {
                    Snackbar snackbar = Snackbar
                            .make(snackbaranchor, R.string.new_chat_message, Snackbar.LENGTH_INDEFINITE)
                            .setAction("SCROLL", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    chatListView.setSelection(position);
                                }
                            });
                    snackbar.show();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview = container;
        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.native_chat_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View v = getView();

        snackbaranchor = v.findViewById(R.id.snackbaranchor);

        ircClient = IRCClient.getInstance();

        final String pass;

        if (this.getArguments() == null) {
            nick = ircClient.getNick();
            server = ircClient.getServer();
            pass = ircClient.getPassword();
            channel = ircClient.getChannels()[0];
        } else {
            nick = this.getArguments().getString("nick");
            server = this.getArguments().getString("server");
            pass = this.getArguments().getString("pass");
            channel = this.getArguments().getString("channel").toLowerCase();
        }

        final int port = 6667;

        chatActivity = NativeChatFragment.this;

        chatListView = (ListView) v.findViewById(R.id.chatlist);

        chatlines = new ArrayList<>();
        if (ircClient.chatlinesBackground != null) {
            chatlines.addAll(ircClient.chatlinesBackground);
        }

        chatAdapter = new ArrayAdapter<SpannableString>(getContext(), R.layout.chatline, chatlines);
        chatListView.setAdapter(chatAdapter);


        // Enable debugging output.
        ircClient.setVerbose(true);

        // Connect to the IRC server.
        final Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (!ircClient.isConnected()) {
                        if (!pass.isEmpty()) {
                            ircClient.setAutoNickChange(false);
                            ircClient.connect(server, port, pass);
                        } else {
                            ircClient.connect(server, port);
                            ircClient.setAutoNickChange(true);
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // do nothing
                        }
                        ircClient.joinChannel(channel);

                    } else if (ircClient.getServer().equals(server)) {
                        String[] channels = ircClient.getChannels();
                        int l = channels.length;
                        int i = 0;
                        int c = -1;
                        while (i < l) {
                            if (!channels[i].equals(channel)) {
                                ircClient.partChannel(channels[i]);
                            } else if (channels[i].equals(channel)) {
                                c = 1;
                            }
                            i++;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // do nothing
                        }
                        if (c == -1) {
                            ircClient.joinChannel(channel);
                        }

                        updateChatline();
                    } else {
                        ircClient.quitServer();
                        ircClient.disconnect();

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // do nothing
                        }

                        if (!pass.isEmpty()) {
                            ircClient.setAutoNickChange(false);
                            ircClient.connect(server, port, pass);
                        } else {
                            ircClient.setAutoNickChange(true);
                            ircClient.connect(server, port);
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // do nothing
                        }
                        ircClient.joinChannel(channel);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    ircClient.addChatline(IRCClient.MessageType.ON_ERROR, "Error", "IO error, try again");
                    updateChatline();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException f) {
                        // do nothing
                    }
                    disconnectAndGoBackToLogin();
                } catch (NickAlreadyInUseException e) {
                    e.printStackTrace();
                    ircClient.addChatline(IRCClient.MessageType.ON_ERROR, "Error", "Nick already in use");
                    updateChatline();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException f) {
                        // do nothing
                    }
                    disconnectAndGoBackToLogin();
                } catch (IrcException e) {
                    e.printStackTrace();
                    ircClient.addChatline(IRCClient.MessageType.ON_ERROR, "Error", "error connecting to IRC, try again");
                    updateChatline();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException f) {
                        // do nothing
                    }
                    disconnectAndGoBackToLogin();
                }
            }
        });
        thread.start();

        final ImageButton sendButton = (ImageButton) v.findViewById(R.id.sendButton);
        sendButton.setEnabled(false);
        sendButton.setAlpha((float) 0.3);
        chatInputEditText = (EditText) v.findViewById(R.id.chatInput);

        chatInputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    handled = true;
                }
                return handled;
            }
        });

        chatInputEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        chatInputEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().length() == 0) {
                    sendButton.setEnabled(false);
                    sendButton.setAlpha((float) 0.3);
                } else {
                    sendButton.setEnabled(true);
                    sendButton.setAlpha((float) 0.9);
                }


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendMessage();
            }
        });

    }

    public void disconnectAndGoBackToLogin() {
        ircClient.disconnect();
        ircClient.dispose();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException f) {
            // do nothing
        }

        ChatLoginFragment newFragment = new ChatLoginFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        if (this.getArguments() != null) {
            Bundle bundle = new Bundle();
            bundle.putString("channelName", this.getArguments().getString("channelName"));
            bundle.putString("streamService", this.getArguments().getString("streamService"));
            newFragment.setArguments(bundle);
        }

        transaction.replace(rootview.getId(), newFragment);
        transaction.commit();
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_native_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.disconnect:
                disconnectAndGoBackToLogin();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendMessage() {
        chatInput = chatInputEditText.getText().toString();
        if (!chatInput.isEmpty()) {
            ircClient.sendMessage(channel, chatInput);
            ircClient.addChatline(IRCClient.MessageType.DEFAULT, nick, chatInput);
            updateChatline();
            chatInputEditText.setText("");
            hideKeyboard(chatInputEditText);
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}




