package com.example.reesit.services;

import com.example.reesit.models.Merchant;
import com.example.reesit.models.Tag;
import com.example.reesit.models.User;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class TagService {
    private static final int TAG_SUGGESTIONS_LIMIT = 10 ;

    public interface AddTagCallback {
        public void done(Tag newTag, Exception e);
    }
    public interface GetTagsCallback {
        public void done(List<Tag> tags, Exception e);
    }

    public static void getTags(User user, GetTagsCallback callback){
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(Tag.PARSE_CLASS_NAME);
        query.whereEqualTo(Tag.KEY_USER, user.getParseUser());
        query.setLimit(TAG_SUGGESTIONS_LIMIT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
                    List<Tag> tags = new ArrayList<>();
                    for (ParseObject object: objects){
                        tags.add(Tag.fromParseObject(object));
                    }
                    callback.done(tags, null);
                } else {
                    callback.done(null, e);
                }
            }
        });
    }

    public static void getTag(String tagName, User user, GetTagsCallback callback){
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(Tag.PARSE_CLASS_NAME);
        query.whereEqualTo(Tag.KEY_SLUG, Tag.getSlugFromName(tagName));
        query.whereEqualTo(Tag.KEY_USER, user.getParseUser());
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
                    List<Tag> tags = new ArrayList<>();
                    if (objects.size() > 0){
                        tags.add(Tag.fromParseObject(objects.get(0)));
                    }
                    callback.done(tags, null);
                } else {
                    callback.done(null, e);
                }
            }
        });
    }

    public static void addTag(Tag tag, User user, AddTagCallback callback){
        if (tag.getId() == null){
            // check that a tag with the same slug doesn't exist
            getTag(tag.getName(), user, new GetTagsCallback() {
                @Override
                public void done(List<Tag> tags, Exception e) {
                    if (e == null){
                        if (tags.size() == 0){
                            ParseObject object = new ParseObject(Tag.PARSE_CLASS_NAME);
                            object.put(Tag.KEY_NAME, tag.getName());
                            object.put(Tag.KEY_SLUG, tag.getSlug());
                            object.put(Tag.KEY_USER, user.getParseUser());
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    callback.done(Tag.fromParseObject(object), e);
                                }
                            });
                        } else {
                            callback.done(tags.get(0), null);
                        }
                    } else {
                        callback.done(null, new ParseException(new Exception("Error checking if merchant with slug already exists",e)));
                    }
                }
            });
        } else {
            callback.done(tag, null);
        }
    }
    public static void getSuggestedTags(String input, User user, GetTagsCallback callback){
        if (input.length() == 0){
            callback.done(new ArrayList<>(), null);
        }
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(Tag.PARSE_CLASS_NAME);
        query.whereContains(Tag.KEY_SLUG, Tag.getSlugFromName(input));
        query.whereEqualTo(Tag.KEY_USER, user.getParseUser());
        query.setLimit(TAG_SUGGESTIONS_LIMIT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
                    List<Tag> tags = new ArrayList<>();
                    for (ParseObject tagObject: objects){
                        tags.add(Tag.fromParseObject(tagObject));
                    }
                    callback.done(tags, null);
                } else {
                    callback.done(null, e);
                }
            }
        });
    }
}
