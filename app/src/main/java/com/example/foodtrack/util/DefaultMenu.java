package com.example.foodtrack.util;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;

import com.example.foodtrack.DashboardActivity;
import com.example.foodtrack.MainActivity;
import com.example.foodtrack.ProfileActivity;
import com.example.foodtrack.R;
import com.google.firebase.auth.FirebaseAuth;

public class DefaultMenu {

    private static final String LOG_TAG = "DefaultMenu";

    public static boolean handleMenuSelection(Activity activity, MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logoutButton) {
            Log.d(LOG_TAG, "Log out clicked!");
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(activity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
            return true;

        } else if (id == R.id.dashboardButton) {
            if (!activity.getClass().equals(DashboardActivity.class)) {
                Log.d(LOG_TAG, "Navigating to Dashboard");
                Intent intent = new Intent(activity, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                activity.finish();
            } else {
                Log.d(LOG_TAG, "Already in Dashboard");
            }
            return true;

        } else if (id == R.id.profileButton) {
            if (!activity.getClass().equals(ProfileActivity.class)) {
                Log.d(LOG_TAG, "Navigating to Profile");
                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                activity.finish();
            } else {
                Log.d(LOG_TAG, "Already in Profile");
            }
            return true;
        }

        return false;
    }
}
