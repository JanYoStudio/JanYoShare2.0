package pw.janyo.janyoshare.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import pw.janyo.janyoshare.R;
import pw.janyo.janyoshare.adapter.AppAdapter;
import pw.janyo.janyoshare.classes.InstallAPP;
import pw.janyo.janyoshare.util.AppManager;
import pw.janyo.janyoshare.util.JanYoFileUtil;
import vip.mystery0.tools.logs.Logs;


public class AppFragment extends Fragment {
    public static AppFragment newInstance(int type) {
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        AppFragment fragment = new AppFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private static final String TAG = "AppFragment";
    private int type = 0;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<InstallAPP> list = new ArrayList<>();
    private AppAdapter appAdapter = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getInt("type");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logs.i(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_app, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        appAdapter = new AppAdapter(getActivity(), list);
        recyclerView.setAdapter(appAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });
        return view;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    private void refreshList() {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> subscriber) throws Exception {
                list.clear();
                list.addAll(AppManager.getInstallAPPList(getActivity(), type));
                subscriber.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .unsubscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        swipeRefreshLayout.setRefreshing(true);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logs.wtf(TAG, "onError: ", e);
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onComplete() {
                        Logs.i(TAG, "onComplete: ");
                        Logs.i(TAG, "onComplete: " + list.size());
                        appAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    public void loadCacheList() {
        Observable.create(new ObservableOnSubscribe<List<InstallAPP>>() {
            @Override
            public void subscribe(ObservableEmitter<List<InstallAPP>> subscriber) throws Exception {
                while (true) {
                    if (appAdapter != null)
                        break;
                    Thread.sleep(200);
                }
                String fileName;
                switch (type) {
                    case AppManager.USER:
                        fileName = JanYoFileUtil.USER_LIST_FILE;
                        break;
                    case AppManager.SYSTEM:
                        fileName = JanYoFileUtil.SYSTEM_LIST_FILE;
                        break;
                    default:
                        Logs.e(TAG, "subscribe: 应用类型错误");
                        fileName = "";
                        break;
                }
                File file = new File(getActivity().getExternalCacheDir(), fileName);
                subscriber.onNext(JanYoFileUtil.getListFromFile(file, InstallAPP.class));
                subscriber.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .unsubscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<InstallAPP>>() {
                    private List<InstallAPP> installAPPList = new ArrayList<>();

                    @Override
                    public void onSubscribe(Disposable d) {
                        Logs.i(TAG, "onSubscribe: ");
                    }

                    @Override
                    public void onNext(List<InstallAPP> installAPPList) {
                        this.installAPPList.clear();
                        this.installAPPList.addAll(installAPPList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        refreshList();
                    }

                    @Override
                    public void onComplete() {
                        if (installAPPList.size() != 0) {
                            list.clear();
                            list.addAll(installAPPList);
                            appAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        } else
                            refreshList();
                    }
                });
    }
}
