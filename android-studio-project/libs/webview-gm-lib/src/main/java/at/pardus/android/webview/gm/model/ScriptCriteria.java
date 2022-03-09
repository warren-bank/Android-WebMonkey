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

import at.pardus.android.webview.gm.util.CriterionMatcher;

/**
 * Immutable object containing a user script's matching criteria regarding URLs.
 * 
 * @see <a href="http://wiki.greasespot.net/Metadata_Block">Metadata Block</a>
 */
public class ScriptCriteria extends ScriptId {

  private String[] exclude;

  private String[] include;

  private String[] match;

  public ScriptCriteria(String name, String namespace, String[] exclude,
      String[] include, String[] match) {
    super(name, namespace);
    this.exclude = exclude;
    this.include = include;
    this.match = match;
  }

  /**
   * Checks if a URL matches the criteria of this object.
   * 
   * @param url
   *            the URL to test
   * @return true if the URL does not match any of the exclude patterns and
   *         does match one of the patterns in include or match (or include
   *         and match do not contain any patterns), false else
   */
  public boolean testUrl(String url) {
    if (exclude != null) {
      for (String pattern : exclude) {
        if (CriterionMatcher.test(pattern, url)) {
          return false;
        }
      }
    }
    if ((include == null || include.length == 0)
        && (match == null || match.length == 0)) {
      return true;
    }
    if (include != null) {
      for (String pattern : include) {
        if (CriterionMatcher.test(pattern, url)) {
          return true;
        }
      }
    }
    if (match != null) {
      for (String pattern : match) {
        if (CriterionMatcher.test(pattern, url)) {
          return true;
        }
      }
    }
    return false;
  }

  public String[] getExclude() {
    return exclude;
  }

  public String[] getInclude() {
    return include;
  }

  public String[] getMatch() {
    return match;
  }

}
