import React from 'react';
import ReactDOM from 'react-dom/server';
import { TemplateCompilerComponent } from '@sqs/visitor-react-components';

const componentName = process.argv[2];
let componentProps;
try {
  componentProps = JSON.parse(process.argv[3]);
} catch {
  componentProps = {};
}

const htmlOutput = ReactDOM.renderToString(
  <TemplateCompilerComponent name={componentName} props={componentProps} />
);

process.stdout.write(htmlOutput);
process.exit();
