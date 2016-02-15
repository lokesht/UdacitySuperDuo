package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;


public class About extends Fragment {

    public About() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        TextView tvVersion = (TextView) rootView.findViewById(R.id.tv_version);
        TextView tvHeader = (TextView) rootView.findViewById(R.id.tv_header);
        TextView tvDescription = (TextView) rootView.findViewById(R.id.tv_description);

        PackageInfo pInfo = null;
        try {
            pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            String version = pInfo.versionName;
            int verCode = pInfo.versionCode;

            tvVersion.setText("Version: " + version + "\n" + "Version Code: " + verCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        tvHeader.setText(getString(R.string.header));
        tvDescription.setText(getString(R.string.description));

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.about);
    }

}
