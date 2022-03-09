package com.github.warren_bank.webmonkey;

import at.pardus.android.webview.gm.util.CriterionMatcher;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TestPatternActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test_pattern);

    TextView patternField = (TextView) findViewById(R.id.patternField);
    TextView urlField     = (TextView) findViewById(R.id.urlField);
    Button executeButton  = (Button) findViewById(R.id.executeButton);

    executeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        String pattern = patternField.getText().toString().trim();
        String url     = urlField.getText().toString().trim();

        if (pattern.length() > 0 && url.length() > 0) {
          int msg_id = CriterionMatcher.test(pattern, url)
            ? R.string.testpatternactivity_result_is_match
            : R.string.testpatternactivity_result_no_match;

          Toast.makeText(TestPatternActivity.this, getString(msg_id), Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

}
