package fi.konstal.bullet_your_life.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fi.konstal.bullet_your_life.App;
import fi.konstal.bullet_your_life.R;
import fi.konstal.bullet_your_life.activities.EditCardActivity;
import fi.konstal.bullet_your_life.data.CardRepository;
import fi.konstal.bullet_your_life.data.NoteCard;
import fi.konstal.bullet_your_life.daycard_recycler_view.RecyclerItemClickListener;
import fi.konstal.bullet_your_life.notes_recycler_view.NoteCardViewAdapter;
import fi.konstal.bullet_your_life.util.PopUpHandler;
import fi.konstal.bullet_your_life.view_models.NotesViewModel;


/**
 * Fragment that displays all applications NoteCards
 *
 * @author Konsta Lehtinen
 * @author KontaL
 * @version 1.0
 * @since 1.0
 */
public class NotesFragment extends Fragment {
    private static final String TAG = "NotesFragment";
    @Inject
    CardRepository cardRepository;
    @BindView(R.id.notecards_recycler_view)
    RecyclerView recyclerView;
    private Unbinder unbinder; //ButterKnife lifecycle stuff
    private NoteCardViewAdapter adapter;
    private NotesViewModel viewModel;

    public NotesFragment() {
    }

    // TODO: Rename and change types and number of parameters
    public static NotesFragment newInstance(String param1, String param2) {
        NotesFragment fragment = new NotesFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setup Dagger2
        ((App) getActivity().getApplication()).getAppComponent().inject(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notes, container, false);
        unbinder = ButterKnife.bind(this, v); // bind ButterKnife to this fragment
        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        viewModel = ViewModelProviders.of(this).get(NotesViewModel.class);
        viewModel.init(cardRepository);
        viewModel.getNoteCards().observe(this, cardList -> {

            if (cardList != null) {
                if (recyclerView.getAdapter() == null) {
                    adapter = new NoteCardViewAdapter(getContext());
                    recyclerView.setAdapter(adapter);
                    adapter.setCardList(cardList);
                } else {
                    adapter.updateCardList(cardList);
                }
            }
        });

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Intent intent = new Intent(getContext(), EditCardActivity.class);
                        intent.putExtra("type", "NoteCard");
                        intent.putExtra("id", adapter.getCardList().get(position).getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        View v = getActivity().getLayoutInflater().inflate(R.layout.popup_window_notecards, null, false);
                        v.findViewById(R.id.popup_icon_delete).setOnClickListener((e) -> {
                            viewModel.deleteCard(position);
                        });

                        PopupWindow pw = new PopupWindow(v, LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT, false);
                        pw.setOutsideTouchable(true);
                        pw.setTouchable(true);

                        pw.showAsDropDown(view, 400, -70);


                        Vibrator vi = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                        vi.vibrate(50);
                    }
                })
        );


        FloatingActionButton fab = getActivity().findViewById(R.id.fab_add_notes);
        fab.setOnClickListener((e) -> {

            // 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            View dialogView = getLayoutInflater().inflate(R.layout.partial_notecard_dialog, null);
            builder.setView(dialogView);

            builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    TextView tv = dialogView.findViewById(R.id.NoteCard_new_title);
                    cardRepository.insertNoteCards(new NoteCard(tv.getText().toString()));
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        /*  INIT TOOLBAR
            This is almost exactly the same code as in futureLogsFragment, only ID's are changed*/
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar1);
        toolbar.inflateMenu(R.menu.collapsing_toolbar_items1);
        View popupOpener = getActivity().findViewById(R.id.popup_open1);
        popupOpener.setOnClickListener(new PopUpHandler(getContext(), popupOpener));
        CollapsingToolbarLayout mCollapsingToolbarLayout = getActivity().findViewById(R.id.collapsing_toolbar_layout1);
        mCollapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        mCollapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        final Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "raleway_regular.ttf");
        mCollapsingToolbarLayout.setCollapsedTitleTypeface(tf);
        mCollapsingToolbarLayout.setExpandedTitleTypeface(tf);

        AppBarLayout appBarLayout = getActivity().findViewById(R.id.app_bar_layout1);
        Drawable drawable = toolbar.getMenu().getItem(0).getIcon();
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    //collapse map
                    isShow = true;
                    drawable.setColorFilter(getResources().getColor(R.color.font_black), PorterDuff.Mode.SRC_ATOP);
                } else if (isShow) {
                    //expanded map
                    isShow = false;
                    drawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
