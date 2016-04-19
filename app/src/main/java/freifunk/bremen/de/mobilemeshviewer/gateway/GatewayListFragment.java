package freifunk.bremen.de.mobilemeshviewer.gateway;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.common.base.Optional;
import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import freifunk.bremen.de.mobilemeshviewer.R;
import freifunk.bremen.de.mobilemeshviewer.SwipeRefreshListRoboFragment;
import freifunk.bremen.de.mobilemeshviewer.alarm.AlarmController;
import freifunk.bremen.de.mobilemeshviewer.event.GatewayListUpdatedEvent;
import freifunk.bremen.de.mobilemeshviewer.gateway.model.Gateway;


public class GatewayListFragment extends SwipeRefreshListRoboFragment implements LoaderManager.LoaderCallbacks<List<Gateway>> {

    @Inject
    private GatewayListLoader gatewayListLoader;
    @Inject
    private AlarmController alarmController;
    private ArrayAdapter<Gateway> adapter;
    private boolean visible;
    private Optional<Snackbar> snackbarOptional = Optional.absent();

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        gatewayListLoader.onContentChanged();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
        setEmptyText(getActivity().getString(R.string.list_no_gateways));
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<Gateway>());
        setListAdapter(adapter);
        setListShown(false);

        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                alarmController.sendAlarmImmediately();
            }
        });

        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.removeItem(R.id.action_search);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        visible = isVisibleToUser;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Gateway gateway = (Gateway) l.getItemAtPosition(position);
        final Intent intent = new Intent(this.getActivity(), GatewayActivity.class);
        intent.putExtra(GatewayActivity.BUNDLE_GATEWAY, gateway);
        startActivity(intent);
    }

    @Override
    public Loader<List<Gateway>> onCreateLoader(int id, Bundle args) {
        gatewayListLoader.reset();
        return gatewayListLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Gateway>> loader, List<Gateway> data) {
        adapter.clear();
        adapter.addAll(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Gateway>> loader) {
        adapter.clear();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onGatewayListUpdated(GatewayListUpdatedEvent event) {
        gatewayListLoader.onContentChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGatewayListUpdatedMain(GatewayListUpdatedEvent event) {
        // Hide refresh progress indicator from SwipeRefreshLayout
        if (isRefreshing()) {
            setRefreshing(false);
        }

        // Hide refresh progress indicator from ListView
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }

        // Inform user with SnackBar
        if (visible && (!snackbarOptional.isPresent() || !snackbarOptional.get().isShown())) {
            final Snackbar snackbar;
            if (event.isSuccess()) {
                snackbar = Snackbar.make(getListView(), R.string.snackbar_gateway_success, Snackbar.LENGTH_SHORT);
            } else {
                snackbar = Snackbar.make(getListView(), R.string.snackbar_gateway_fail, Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.snackbar_button_OK, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
            snackbar.show();
            snackbarOptional = Optional.of(snackbar);
        }
    }
}