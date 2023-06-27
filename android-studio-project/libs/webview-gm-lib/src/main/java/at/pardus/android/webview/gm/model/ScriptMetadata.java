/*
 *    Copyright 2012 Werner Bayer
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package at.pardus.android.webview.gm.model;

/**
 * Immutable object containing information about a user script's metadata block.
 * 
 * @see <a href="http://wiki.greasespot.net/Metadata_Block">Metadata Block</a>
 */
public class ScriptMetadata extends ScriptCriteria {

  public static final String RUNATSTART = "document-start";

  public static final String RUNATEND = "document-end";

  private String description;

  private String downloadurl;

  private String updateurl;

  private String installurl;

  private String icon;

  private String runAt;

  private boolean unwrap;

  private String version;

  private ScriptRequire[] requires;

  private ScriptResource[] resources;

  private boolean enabled;

  public ScriptMetadata(String name, String namespace, String[] exclude,
      String[] include, String[] match, String description,
      String downloadurl, String updateurl, String installurl,
      String icon, String runAt, boolean unwrap, String version,
      ScriptRequire[] requires, ScriptResource[] resources, boolean enabled) {
    super(name, namespace, exclude, include, match);
    this.description = description;
    this.downloadurl = downloadurl;
    this.updateurl = updateurl;
    this.installurl = installurl;
    this.icon = icon;
    this.runAt = runAt;
    this.unwrap = unwrap;
    this.version = version;
    this.requires = requires;
    this.resources = resources;
    this.enabled = enabled;
  }

  public String getDescription() {
    return description;
  }

  public String getDownloadurl() {
    return downloadurl;
  }

  public String getUpdateurl() {
    return updateurl;
  }

  public String getInstallurl() {
    return installurl;
  }

  public String getIcon() {
    return icon;
  }

  public String getRunAt() {
    return runAt;
  }

  public boolean isUnwrap() {
    return unwrap;
  }

  public String getVersion() {
    return version;
  }

  public ScriptRequire[] getRequires() {
    return requires;
  }

  public ScriptResource[] getResources() {
    return resources;
  }

  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append(enabled ? "[enabled]" : "[disabled]");
    sb.append(" ");
    sb.append(super.toString());

    if ((description != null) && !description.isEmpty()) {
      sb.append(": ");
      sb.append(description);
    }

    return sb.toString();
  }

}
