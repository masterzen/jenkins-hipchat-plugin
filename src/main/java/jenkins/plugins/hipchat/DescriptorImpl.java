package jenkins.plugins.hipchat;

import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

    public class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private String token;
        private String room;
        private String buildServerUrl;
        private String sendAs;
        private boolean smartNotifications;
        private boolean startNotification;

        public DescriptorImpl() {
            super(HipChatNotifier.class);
            load();
        }

        public String getToken() {
            return token;
        }

        public String getRoom() {
            return room;
        }

        public String getBuildServerUrl() {
            return buildServerUrl;
        }

        public String getSendAs() {
            return sendAs;
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public Publisher newInstance(StaplerRequest request, JSONObject formData) throws FormException {
            String projectRoom = request.getParameter("hipChatRoom");;
            String projectSendAs = request.getParameter("hipChatSendAs");
            
            if (projectRoom == null || projectRoom.trim().length() == 0) {
              projectRoom = room;
            }
            if (projectSendAs == null || projectSendAs.trim().length() == 0) {
              projectSendAs = sendAs;
            }
            
            smartNotifications = request.getParameter("hipChatSmartNotifications") != null;
            startNotification = request.getParameter("hipChatStartNotification") != null;
            return new HipChatNotifier(token, projectRoom, buildServerUrl, projectSendAs, smartNotifications, startNotification);
        }

        @Override
        public boolean configure(StaplerRequest request, JSONObject formData) throws FormException {
            token = request.getParameter("hipChatToken");
            room = request.getParameter("hipChatRoom");
            buildServerUrl = request.getParameter("hipChatBuildServerUrl");
            sendAs = request.getParameter("hipChatSendAs");
            if (buildServerUrl != null && !buildServerUrl.endsWith("/")) {
                buildServerUrl = buildServerUrl + "/";
            }
            smartNotifications = request.getParameter("hipChatSmartNotifications") != null;
            startNotification = request.getParameter("hipChatStartNotification") != null;
            try {
                new HipChatNotifier(token, room, buildServerUrl, sendAs, smartNotifications, startNotification);
            } catch (Exception e) {
                throw new FormException("Failed to initialize notifier - check your global notifier configuration settings", e, "");
            }
            save();
            return super.configure(request, formData);
        }

        @Override
        public String getDisplayName() {
            return "HipChat Notifications";
        }

        public boolean getSmartNotifications()
        {
          return smartNotifications;
        }

        public boolean getStartNotification()
        {
          return startNotification;
        }
}