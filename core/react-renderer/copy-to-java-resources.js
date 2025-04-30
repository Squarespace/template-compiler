import fs from 'fs';

const bundleFileName = 'render-react-component.js';
const bundleFilePath = `./build/${bundleFileName}`;

fs.copyFile(
  bundleFilePath,
  `../src/main/resources/com/squarespace/template/plugins/${bundleFileName}`,
  (err) => {
    if (err) throw err;
  },
);
fs.copyFile(
  bundleFilePath,
  `../build/resources/main/com/squarespace/template/plugins/${bundleFileName}`,
  (err) => {
    if (err) throw err;
  },
);
