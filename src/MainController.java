package musicplayer;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.fxml.FXMLLoader;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.util.Duration;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Optional;
import javafx.scene.layout.Region;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

public class MainController implements Initializable {

    private boolean isSideBarExpanded = true;
    private double expandedWidth = 250;
    private double collapsedWidth = 50;

    @FXML private BorderPane mainWindow;
    @FXML private VBox sideBar;
    @FXML private ImageView sideBarSlideButton;
    @FXML private ImageView playPauseButton;
    @FXML private ImageView nowPlayingArtwork;
    @FXML private Label nowPlayingTitle;
    @FXML private Label nowPlayingArtist;
    @FXML private Slider timeSlider;
    @FXML private Label timePassed;
    @FXML private Label timeRemaining;

    private Animation collapseAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setOnFinished(x -> setSlideDirection());
        }
        protected void interpolate(double frac) {
            double curWidth = collapsedWidth + (expandedWidth - collapsedWidth) * (1.0 - frac);
            slide(curWidth);
        }
    };

    private Animation expandAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setOnFinished(x -> {setVisibility(true); setSlideDirection();});
        }
        protected void interpolate(double frac) {
            double curWidth = collapsedWidth + (expandedWidth - collapsedWidth) * (frac);
            slide(curWidth);
        }
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        timeSlider.valueChangingProperty().addListener(
            (value, wasChanging, isChanging) -> {

                if (wasChanging) {

                    int quarterSeconds = (int) Math.round(timeSlider.getValue());
                    timeSlider.setValue(quarterSeconds);
                    MusicPlayer.seek(quarterSeconds / 4);
                }
            }
        );
    }

    @FXML
    private void selectView(Event e) {

        HBox eventSource = ((HBox)e.getSource());

        Optional<Node> previous = sideBar.getChildren().stream()
            .filter(x -> x.getStyleClass().get(0).equals("sideBarItemSelected")).findFirst();

        if (previous.isPresent()) {
            HBox previousItem = (HBox)previous.get();
            previousItem.getStyleClass().setAll("sideBarItem");
        }

        ObservableList<String> styles = eventSource.getStyleClass();

        if (styles.get(0).equals("sideBarItem")) {
            styles.setAll("sideBarItemSelected");
            loadView(eventSource);
        } else if (styles.get(0).equals("bottomBarItem")) {
            loadView(eventSource);
        }
    }

    @FXML
    private void slideSideBar() {

        if (isSideBarExpanded) {
            collapseSideBar();
        } else {
            expandSideBar();
        }
    }

    @FXML
    public void playPause() {

        if (MusicPlayer.isPlaying()) {
            MusicPlayer.pause();
        } else {
            MusicPlayer.play();
        }
    }

    public void updatePlayPauseIcon() {

        Image icon;

        if (MusicPlayer.isPlaying()) {

            icon = new Image(this.getClass().getResource(Resources.IMG + "playIcon.png").toString());
            playPauseButton.setImage(icon);

        } else {

            icon = new Image(this.getClass().getResource(Resources.IMG + "pauseIcon.png").toString());
            playPauseButton.setImage(icon);
        }
    }

    public void updateNowPlayingButton() {

        Song song = MusicPlayer.getNowPlaying();

        nowPlayingTitle.setText(song.getTitle());
        nowPlayingArtist.setText(song.getArtist());

        Image artwork = song.getArtwork();
        nowPlayingArtwork.setImage(artwork);
    }

    public void initializeTimeSlider() {

        Song song = MusicPlayer.getNowPlaying();
        timeSlider.setMin(0);
        timeSlider.setMax(song.getLength().getSeconds() * 4);
        timeSlider.setValue(0);
        timeSlider.setBlockIncrement(1);
    }

    public void updateTimeSlider() {

        timeSlider.increment();
    }

    public void initializeTimeLabels() {

        timePassed.setText("0:00");
        timeRemaining.setText(MusicPlayer.getNowPlaying().getLengthAsString());
    }

    public void updateTimeLabels() {

        timePassed.setText(MusicPlayer.getTimePassed());
        timeRemaining.setText(MusicPlayer.getTimeRemaining());
    }

    private void loadView(HBox eventSource) {

        try {

            String fileName = Resources.FXML + eventSource.getId() + ".fxml";
            Node view = (Node)FXMLLoader.load(this.getClass().getResource(fileName));
            mainWindow.setCenter(view);

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    private void collapseSideBar() {

        if (expandAnimation.statusProperty().get() == Animation.Status.STOPPED
            && collapseAnimation.statusProperty().get() == Animation.Status.STOPPED) {

                setVisibility(false);
                collapseAnimation.play();
        }
    }

    private void expandSideBar() {

        if (expandAnimation.statusProperty().get() == Animation.Status.STOPPED
            && collapseAnimation.statusProperty().get() == Animation.Status.STOPPED) {

                expandAnimation.play();
        }
    }

    private void slide(double curWidth) {

        sideBar.setPrefWidth(curWidth);
        for (Node n : sideBar.getChildren()) {
            if (n instanceof HBox) {
                for (Node m : ((HBox)n).getChildren()) {
                    if (m instanceof Label) {
                        m.setTranslateX(-expandedWidth + curWidth);
                    }
                }
            }
        }
    }

    private void setVisibility(boolean isVisible) {

        for (Node n : sideBar.getChildren()) {
            if (n instanceof HBox) {
                for (Node m : ((HBox)n).getChildren()) {
                    if (m instanceof Label) {
                        m.setOpacity(isVisible ? 1 : 0);
                    }
                }
            }
        }
    }

    private void setSlideDirection() {

        isSideBarExpanded = !isSideBarExpanded;
        sideBarSlideButton.setImage(new Image(this.getClass().getResource(Resources.IMG
            + (isSideBarExpanded
            ? "leftArrowIcon.png"
            : "rightArrowIcon.png")
        ).toString()));
    }
}