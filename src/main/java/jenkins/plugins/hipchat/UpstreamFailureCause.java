package jenkins.plugins.hipchat;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jenkins.model.Jenkins;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import hudson.Util;
import hudson.model.Item;
import hudson.model.Result;
import hudson.model.TopLevelItem;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.User;

public class UpstreamFailureCause
{
  private static final Logger logger = Logger.getLogger(ActiveNotifier.class.getName());

  private final AbstractBuild build;

  public UpstreamFailureCause(AbstractBuild build)
  {
    this.build = build;
  }

  public Set<User> getUpstream()
  {
    if (build.getResult() == Result.SUCCESS || build.getCause(Cause.UpstreamCause.class) == null) {
      logger.info("upstream: build is success or no cause recorded");
      return Collections.emptySet();
    }

    ArrayList<Cause.UpstreamCause> upstreamCauses = getUpstreamCauses(build);
    for(Cause.UpstreamCause cause: upstreamCauses) {
      logger.info("upstream: found cause project " + cause.getUpstreamProject() + " -> " + cause.getUpstreamBuild());
    }
    
    return getCulprits(upstreamCauses);
  }


  private Set<User> getCulprits(ArrayList<Cause.UpstreamCause> upstreamCauses)
  {

    Set<User> culprits = new HashSet<User>();

    for (Cause.UpstreamCause cause : upstreamCauses) {
      if (cause != null) {
        logger.info("upstream culprits: cause " + cause.getUpstreamProject() + " -> " + cause.getUpstreamBuild());
        Item item = Jenkins.getInstance().getItemByFullName(cause.getUpstreamProject());
        if (item instanceof AbstractProject) {
          logger.info("upstream culprits: found item " + item.getFullDisplayName());
          AbstractBuild build = ((AbstractProject)item).getBuildByNumber(cause.getUpstreamBuild());
          if (build != null) {
            logger.info("upstream culprits: found build " + build.getDisplayName());
            logger.info("upstream culprits: adding culprit " + build.getCulprits());
            culprits.addAll(build.getCulprits());
          }
        }
      }
    }

    return culprits;
  }

  private Collection<String> getProjectNames(Collection<AbstractProject> projects)
  {
    Collection<String> namesCollection = Collections2.transform(projects, new Function<AbstractProject, String>() {
      public String apply(AbstractProject from)
      {
        return from.getName();
      }
    });

    return namesCollection;
  }

  private ArrayList<AbstractProject> getUpstreamProjects(ArrayList<Cause.UpstreamCause> upstreamCauses)
  {

    ArrayList<AbstractProject> projects = new ArrayList<AbstractProject>();

    for (Cause.UpstreamCause cause : upstreamCauses) {
      String project = cause.getUpstreamProject();
      projects.add((AbstractProject) Jenkins.getInstance().getItem(project));
    }

    return projects;
  }

  private ArrayList<Cause.UpstreamCause> getUpstreamCauses(AbstractBuild<?, ?> build)
  {
    Cause.UpstreamCause buildCause = build.getCause(Cause.UpstreamCause.class);

    ArrayList<Cause.UpstreamCause> causes = new ArrayList<Cause.UpstreamCause>();

    causes.add(buildCause);

    getUpstreamCauses(buildCause.getUpstreamCauses(), causes);

    return causes;
  }

  private void getUpstreamCauses(List<Cause> upstreamCauses, final ArrayList<Cause.UpstreamCause> causes)
  {

    causes.addAll(CollectionUtils.select(upstreamCauses, new Predicate() {
      public boolean evaluate(Object o)
      {
        if (o instanceof Cause.UpstreamCause) {
          Cause.UpstreamCause cause = (Cause.UpstreamCause) o;
          getUpstreamCauses(cause.getUpstreamCauses(), causes);
        }

        return o instanceof Cause.UpstreamCause;
      }
    }));
  }

}
