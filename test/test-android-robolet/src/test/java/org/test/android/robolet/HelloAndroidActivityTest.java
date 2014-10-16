package org.test.android.robolet;

import android.widget.TextView;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class HelloAndroidActivityTest {

    @Test
    public void shouldDisplayHello() throws Exception {
        HelloAndroidActivity activity =  new HelloAndroidActivity();
        activity.onCreate(null);
        String hello = activity.getResources().getString(R.string.hello);
        TextView helloView = (TextView) activity.findViewById(R.id.hello_view);
        assertEquals(helloView.getText(), hello);
    }
}