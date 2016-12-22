/*
 * Knot.x - Reactive microservice assembler - HTML Fragment Splitter
 *
 * Copyright (C) 2016 Cognifide Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cognifide.knotx.splitter;

import com.google.common.collect.Lists;

import com.cognifide.knotx.fragments.Fragment;

import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HtmlFragmentSplitter implements FragmentSplitter {


  private static final String SNIPPET_IDENTIFIER_NAME = "data-api-type";

  private static final String ANY_SNIPPET_PATTERN = "(?is).*<script\\s+" + SNIPPET_IDENTIFIER_NAME + ".*";
  private static final Pattern SNIPPET_PATTERN =
      Pattern.compile("<script\\s+" + SNIPPET_IDENTIFIER_NAME + "\\s*=\\s*\"([A-Za-z0-9-]+)\"[^>]*>.+?</script>", Pattern.DOTALL);
  private static final String FRAGMENT_IDENTIFIERS_SEPARATOR = ",";

  @Override
  public List<Fragment> split(String html) {
    List<Fragment> fragments = Lists.newLinkedList();
    if (html.matches(ANY_SNIPPET_PATTERN)) {
      Matcher matcher = SNIPPET_PATTERN.matcher(html);
      int idx = 0;
      while (matcher.find()) {
        MatchResult matchResult = matcher.toMatchResult();
        if (idx < matchResult.start()) {
          fragments.add(toRaw(html, idx, matchResult.start()));
        }
        fragments.add(toSnippet(matchResult.group(1).intern().split(FRAGMENT_IDENTIFIERS_SEPARATOR), html, matchResult.start(), matchResult.end()));
        idx = matchResult.end();
      }
      if (idx < html.length()) {
        fragments.add(toRaw(html, idx, html.length()));
      }
    } else {
      fragments.add(toRaw(html, 0, html.length()));
    }
    return fragments;
  }

  private Fragment toRaw(String html, int startIdx, int endIdx) {
    return Fragment.raw(html.substring(startIdx, endIdx));
  }

  private Fragment toSnippet(String[] ids, String html, int startIdx, int endIdx) {
    return Fragment.snippet(Arrays.asList(ids), html.substring(startIdx, endIdx));
  }
}
