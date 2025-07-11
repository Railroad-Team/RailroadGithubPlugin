package io.github.railroad.github.ui;

import com.sun.javafx.application.HostServicesDelegate;
import io.github.railroad.core.localization.LocalizationServiceLocator;
import io.github.railroad.core.logger.LoggerServiceLocator;
import io.github.railroad.core.ui.RRButton;
import io.github.railroad.core.ui.RRListView;
import io.github.railroad.core.ui.RRVBox;
import io.github.railroad.core.ui.localized.LocalizedLabel;
import io.github.railroad.github.GithubAccount;
import io.github.railroad.github.GithubConnection;
import io.github.railroad.github.http.AccessTokenResponse;
import io.github.railroad.github.http.DeviceCodeResponse;
import io.github.railroad.github.http.GithubRequests;
import io.github.railroad.github.http.UserResponse;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GithubAccountsPane extends RRVBox {
    private final RRListView<GithubAccount> accountsListView = new RRListView<>();

    public GithubAccountsPane(List<GithubAccount> defaultAccounts) {
        this.accountsListView.getItems().addAll(defaultAccounts);
        this.accountsListView.setCellFactory(listView -> new GithubAccountListCell(account -> {
            accountsListView.getItems().remove(account);


        }));

        var addButton = new RRButton("github.button.add_account");
        addButton.setOnAction($ -> showAddAccountDialog());

        var scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(this.accountsListView);
        getChildren().addAll(scrollPane, addButton);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private static void showAddAccountDialog() {
        var dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add GitHub Account");

        var dialogVbox = new RRVBox(20);

        var authorizeButton = new RRButton("github.button.authorize");
        authorizeButton.setOnAction($ -> {
            DeviceCodeResponse deviceCodeResponse = GithubRequests.requestDeviceCode("repo,read:user,user:email,notifications,gist,read:org");
            String verificationUri = deviceCodeResponse.verificationUri();
            String userCode = deviceCodeResponse.userCode();
            String deviceCode = deviceCodeResponse.deviceCode();
            int interval = deviceCodeResponse.interval();

            tryOpenUrl(verificationUri);
            Stage userCodeDialog = createUserCodeDialog(userCode);
            CompletableFuture<AccessTokenResponse> future = pollForAccessToken(deviceCode, interval, dialog);
            userCodeDialog.showAndWait();

            future.thenAccept(response -> {
                if (response instanceof AccessTokenResponse.SuccessResponse successResponse) {
                    String accessToken = successResponse.getAccessToken();
                    if (accessToken.isEmpty()) {
                        LoggerServiceLocator.getInstance().getLogger().error("Received empty access token");
                        return;
                    }

                    UserResponse userResponse = GithubRequests.requestUser(accessToken);
                    if (userResponse == null) {
                        LoggerServiceLocator.getInstance().getLogger().error("Failed to retrieve user information");
                        return;
                    }

                    // TODO
                }
            });
        });

        var scene = new Scene(dialogVbox, 400, 200);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    private static Stage createUserCodeDialog(String userCode) {
        var userCodeDialog = new Stage();
        userCodeDialog.initModality(Modality.APPLICATION_MODAL);
        userCodeDialog.setTitle(LocalizationServiceLocator.getInstance().get("github.dialog.user_code.title"));

        var userCodeVbox = new RRVBox(20);
        var userCodeLabel = new LocalizedLabel("github.label.enter_code_in_browser");

        var codeDisplay = new MFACodeDisplay(userCode);
        userCodeVbox.getChildren().addAll(userCodeLabel, codeDisplay);

        var cancelButton = new RRButton("railroad.generic.cancel");
        cancelButton.setOnAction($ -> userCodeDialog.close());
        userCodeVbox.getChildren().add(cancelButton);

        var scene = new Scene(userCodeVbox, 300, 150);
        userCodeDialog.setScene(scene);
        userCodeDialog.setResizable(false);
        return userCodeDialog;
    }

    private static CompletableFuture<AccessTokenResponse> pollForAccessToken(String deviceCode, int interval, Stage dialog) {
        CompletableFuture<AccessTokenResponse> future = new CompletableFuture<>();

        new Thread(() -> {
            while(!Thread.interrupted()) {
                try {
                    AccessTokenResponse response = GithubRequests.requestAccessToken(deviceCode);
                    if(response instanceof AccessTokenResponse.ErrorResponse errorResponse) {
                        if(errorResponse.getErrorType() == AccessTokenResponse.ErrorType.AUTHORIZATION_PENDING) {
                            Thread.sleep(interval * 1000L);
                            continue;
                        } else if(errorResponse.getErrorType() == AccessTokenResponse.ErrorType.SLOW_DOWN) {
                            Thread.sleep((interval + 5) * 1000L);
                            continue;
                        } else {
                            future.complete(errorResponse);
                            LoggerServiceLocator.getInstance().getLogger().error("Error while polling for access token: {}", errorResponse.getErrorType());
                            break;
                        }
                    }

                    if(response instanceof AccessTokenResponse.SuccessResponse successResponse) {
                        future.complete(successResponse);
                        LoggerServiceLocator.getInstance().getLogger().info("Successfully received access token");

                        Platform.runLater(() -> {
                            Button confirmButton = createConfirmButton(successResponse.getAccessToken(), dialog);
                            var root = new RRVBox();
                            root.getChildren().addAll(new LocalizedLabel("github.dialog.confirm.title"), confirmButton);
                            dialog.setScene(new Scene(root, 300, 150));
                            dialog.showAndWait();
                        });
                        break;
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception exception) {
                    LoggerServiceLocator.getInstance().getLogger().error("Error while polling for access token", exception);
                    return;
                }
            }
        }).start();

        return future;
    }

    private static void tryOpenUrl(String url) {
        boolean desktopWorked = false;
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI.create(url));
                desktopWorked = true;
            } catch (Exception ignored) {}
        }

        if(desktopWorked) return;

        boolean wwwBrowserWorked = false;
        try {
            new ProcessBuilder("x-www-browser", url).start();
            wwwBrowserWorked = true;
        } catch (Exception ignored) {}

        if(wwwBrowserWorked) return;

        HostServicesDelegate hostServices = HostServicesDelegate.getInstance(null);
        if (hostServices != null) {
            try {
                hostServices.showDocument(url);
            } catch (Exception ignored) {}
        }
    }

    private static Button createConfirmButton(String accessToken, Stage dialog) {
        var confirmButton = new RRButton("railroad.generic.confirm");
        confirmButton.setOnAction($ -> {
            if (!accessToken.isEmpty()) {
                var newAccount = new GithubAccount();

                var connection = new GithubConnection(newAccount);
                if (connection.validateProfile()) {
                    // TODO: Get RepositoryManagerService and add the account to it
                }
            } else {
                // Handle empty fields (e.g., show an error message)
            }

            dialog.close();
        });

        return confirmButton;
    }

    public List<GithubAccount> getAccounts() {
        return accountsListView.getItems();
    }

    public void setAccounts(List<GithubAccount> accounts) {
        accountsListView.getItems().setAll(accounts);
    }
}
