package com.groupx.quicknews.forums;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isNotEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.internal.util.Checks.checkNotNull;
import static com.groupx.quicknews.util.Util.atPosition;
import static com.groupx.quicknews.util.Util.childAtPosition;
import static com.groupx.quicknews.util.Util.getCurrentActivity;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.groupx.quicknews.BaseActivity;
import com.groupx.quicknews.ForumActivity;
import com.groupx.quicknews.ForumsListFragment;
import com.groupx.quicknews.R;
import com.groupx.quicknews.util.RecyclerViewIdlingResource;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.Objects;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ForumActivityTest {

    private RecyclerViewIdlingResource idlingResource;
    private RecyclerView recyclerViewComments;
    @Rule
    public ActivityScenarioRule<BaseActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(BaseActivity.class);

    @Before
    public void setUp() {
        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.action_forums), withContentDescription("Forums"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.bottomNavigation),
                                        0),
                                1),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.view_forum),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                0)));
        recyclerView.perform(actionOnItemAtPosition(0, click()));
    }

        @Test
    public void forumListLoadedTest() {
        ViewInteraction commentRecyclerView = onView(withId(R.id.view_comment));
        commentRecyclerView.check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        ViewInteraction editText = onView(
                allOf(withId(R.id.edit_post), withHint("Enter Message"),
                        withParent(allOf(withId(R.id.layout_make_post))),
                        isDisplayed()));
        editText.check(matches(withHint("Enter Message")));

        ViewInteraction button = onView(
                allOf(withId(R.id.button_post), withText("Post"),
                        withParent(allOf(withId(R.id.layout_make_post))),
                        isDisplayed()));
        button.check(matches(isNotEnabled()));
    }

    @Test
    public void postSingleCommentTest() {
        ViewInteraction commentRecyclerView = onView(withId(R.id.view_comment));
        commentRecyclerView.check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        ViewInteraction button = onView(
                allOf(withId(R.id.button_post), withText("Post"),
                        withParent(withId(R.id.layout_make_post)),
                        isDisplayed()));
        ViewInteraction editText = onView(
                allOf(withId(R.id.edit_post), withHint("Enter Message"),
                        withParent(withId(R.id.layout_make_post)),
                        isDisplayed()));

        editText.perform(replaceText("Test Post 1"), closeSoftKeyboard());
        button.check(matches(isEnabled()));
        button.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Activity activity = getCurrentActivity();
        RecyclerView recyclerView = activity.findViewById(R.id.view_comment);
        int count = recyclerView.getAdapter().getItemCount();

        onView(withId(R.id.view_comment))
                .perform(scrollToPosition(count - 1))
                .check(matches(atPosition(count - 1, hasDescendant(
                        allOf(withId(R.id.text_comment), withText("Test Post 1"))))));

        final String[] user = new String[1];
        mActivityScenarioRule.getScenario().onActivity(a -> {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(a);
            user[0] = account.getGivenName() + " " + account.getFamilyName();

        });

        onView(withId(R.id.view_comment))
                .check(matches(atPosition(count - 1, hasDescendant(
                        allOf(withId(R.id.text_user), withText(user[0]))))));

        editText.check(matches(withHint("Enter Message")));
        button.check(matches(isNotEnabled()));
    }

    @Test
    public void postMultipleCommentsTest() {
        ViewInteraction commentRecyclerView = onView(withId(R.id.view_comment));
        commentRecyclerView.check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        ViewInteraction button = onView(
                allOf(withId(R.id.button_post), withText("Post"),
                        withParent(withId(R.id.layout_make_post)),
                        isDisplayed()));
        ViewInteraction editText = onView(
                allOf(withId(R.id.edit_post), withHint("Enter Message"),
                        withParent(withId(R.id.layout_make_post)),
                        isDisplayed()));
        try {
        editText.perform(replaceText("Test Post 2"), closeSoftKeyboard());
        button.perform(click());
        Thread.sleep(2000);

        editText.perform(replaceText("Test Post 3"), closeSoftKeyboard());
        button.perform(click());
        Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        Activity activity = getCurrentActivity();
        RecyclerView recyclerView = activity.findViewById(R.id.view_comment);
        int count = recyclerView.getAdapter().getItemCount();

        onView(withId(R.id.view_comment))
                .perform(scrollToPosition(count - 1))
                .check(matches(atPosition(count - 1, hasDescendant(
                        allOf(withId(R.id.text_comment), withText("Test Post 3"))))));

        onView(withId(R.id.view_comment))
                .perform(scrollToPosition(count - 1))
                .check(matches(atPosition(count - 2, hasDescendant(
                        allOf(withId(R.id.text_comment), withText("Test Post 2"))))));

        editText.check(matches(withHint("Enter Message")));
        button.check(matches(isNotEnabled()));
    }

    //@Test
    public void receiveNewCommentTest() {
        ViewInteraction commentRecyclerView = onView(withId(R.id.view_comment));
        commentRecyclerView.check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        Activity activity = getCurrentActivity();
        RecyclerView recyclerView = activity.findViewById(R.id.view_comment);
        int count = recyclerView.getAdapter().getItemCount();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        onView(withId(R.id.view_comment))
                .perform(scrollToPosition(count - 1))
                .check(matches(atPosition(count - 1, hasDescendant(
                        allOf(withId(R.id.text_comment), withText("Received Test Post"))))));
    }
}