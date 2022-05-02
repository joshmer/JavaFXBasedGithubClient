import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class GithubClientUIController implements Initializable {
    ObservableList<Extras> extrasOl;
    Label detailsLVPlaceHolder;

    @FXML
    private Text accountURL;

    @FXML
    private ImageView avatarIV;

    @FXML
    private ListView<Extras> detailsLV;

    @FXML
    private Button followersBtn;

    @FXML
    private Button followingBtn;

    @FXML
    private TitledPane latestReposTP;

    @FXML
    private Text noReposText;

    @FXML
    private Button publicGistsBtn;

    @FXML
    private Button publicReposBtn;

    @FXML
    private HBox searchHB;

    @FXML
    private TextField searchTF;

    @FXML
    void onSearchHandler(KeyEvent event) throws Exception {
        if (searchTF.getText().trim().equals("")) {
            resetUI();
        } else if (!searchTF.getText().trim().equals("") &&
                event.getCode().equals(KeyCode.ENTER)) {
            ProgressIndicator pIndicator = new ProgressIndicator();
            pIndicator.setPrefSize(20, 20);
            searchHB.getChildren().add(pIndicator);
            searchTF.setDisable(true);

            try {
                HTTPService service = new HTTPService(new URL("https://api.github.com/users/" + searchTF.getText()));
                service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent t) {
                        if (t.getSource().getValue().toString().trim() != "") {
                            try {
                                Object obj = new JSONParser().parse(t.getSource().getValue().toString());
                                JSONObject jo = (JSONObject) obj;
                                if (!(jo.containsKey("message") && jo.containsValue("Not Found"))) {
                                    String avatarURL = (String) jo.get("avatar_url");
                                    long publicRepos = (long) jo.get("public_repos");
                                    long publicGists = (long) jo.get("public_gists");
                                    long followers = (long) jo.get("followers");
                                    long following = (long) jo.get("following");

                                    avatarIV.setImage(new Image(avatarURL));
                                    publicReposBtn.setText("Public Repos " + String.valueOf(publicRepos));
                                    publicGistsBtn.setText("Public Gists " + String.valueOf(publicGists));
                                    followersBtn.setText("Followers " + String.valueOf(followers));
                                    followingBtn.setText("Following " + String.valueOf(following));

                                    List<Extras> extrasList = new ArrayList<>();
                                    extrasList.add(new Extras("Company: " + (String) jo.get("company")));
                                    extrasList.add(new Extras("Website: " + (String) jo.get("website")));
                                    extrasList.add(new Extras("location: " + (String) jo.get("location")));
                                    extrasList.add(new Extras("Member Since: " + (String) jo.get("created_at")));

                                    extrasOl.setAll(extrasList);

                                    loadRepos();

                                    accountURL.setText((String) jo.get("html_url"));
                                }

                            } catch (Exception ex) {
                                searchHB.getChildren().remove(pIndicator);
                                resetUI();
                            }
                        } else {
                            resetUI();
                        }
                        searchHB.getChildren().remove(pIndicator);
                        searchTF.setDisable(false);
                    }
                });
                service.start();
            } catch (Exception ex) {
                searchHB.getChildren().remove(pIndicator);
                resetUI();
            }
        }
    }

    private void loadRepos() {
        try {
            ProgressIndicator pIndicator = new ProgressIndicator();
            pIndicator.setPrefSize(40, 40);
            latestReposTP.setContent(pIndicator);

            HTTPService reposService = new HTTPService(new URL("https://api.github.com/users/" +
                    searchTF.getText() +
                    "/repos?per_page=5&sort=created"));
            reposService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    if (t.getSource().getValue().toString().trim() != "") {
                        try {
                            Object obj = new JSONParser().parse(t.getSource().getValue().toString());
                            JSONArray jo = (JSONArray) obj;
                            if (jo.size() != 0) {
                                var iterator = jo.iterator();

                                VBox repoContainerVB = new VBox();
                                repoContainerVB.setAlignment(Pos.CENTER_LEFT);
                                repoContainerVB.setSpacing(5);

                                HBox[] publicReposHB = new HBox[jo.size()];

                                int repoIndex = 0;
                                while (iterator.hasNext()) {
                                    Object repoObj = new JSONParser().parse(iterator.next().toString());
                                    JSONObject repoJO = (JSONObject) repoObj;
                                    publicReposHB[repoIndex] = new HBox();
                                    publicReposHB[repoIndex].setSpacing(10);
                                    publicReposHB[repoIndex].setAlignment(Pos.CENTER_LEFT);
                                    Pane pane = new Pane();
                                    publicReposHB[repoIndex].getChildren().addAll(
                                            new Text((String) repoJO.get("name")),
                                            pane,
                                            new Button("Stars: " + (long) repoJO.get("stargazers_count")),
                                            new Button("Watchers: " + (long) repoJO.get("watchers_count")),
                                            new Button("Forks: " + (long) repoJO.get("forks")));

                                    HBox.setHgrow(pane, Priority.ALWAYS);
                                    repoContainerVB.getChildren().add(publicReposHB[repoIndex]);

                                    ++repoIndex;
                                }

                                latestReposTP.setContent(repoContainerVB);
                            } else {
                                latestReposTP.setContent(noReposText);
                            }
                        } catch (Exception ex) {
                            latestReposTP.setContent(noReposText);
                        }
                    } else {
                        latestReposTP.setContent(noReposText);
                    }
                }
            });
            reposService.start();
        } catch (Exception ex) {
            latestReposTP.setContent(noReposText);
        }
    }

    private void resetUI() {
        searchTF.setDisable(false);
        publicReposBtn.setText("Public Repos");
        publicGistsBtn.setText("Public Gists");
        followersBtn.setText("Followers");
        followingBtn.setText("Following");
        latestReposTP.setContent(noReposText);
        avatarIV.setImage(new Image(getClass().getResourceAsStream("images/default_avatar.png")));
        accountURL.setText("URL to account");
        extrasOl.clear();
    }

    @Override
    public void initialize(URL param1, ResourceBundle parm2) {
        extrasOl = FXCollections.observableArrayList();

        detailsLVPlaceHolder = new Label("No Content To Display");
        detailsLVPlaceHolder.setFont(Font.font(14));

        detailsLV.setItems(extrasOl);
        detailsLV.setPlaceholder(detailsLVPlaceHolder);

        avatarIV.setImage(new Image(getClass().getResourceAsStream("images/default_avatar.png")));

    }

}
