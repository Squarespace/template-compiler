const React = require('react');
const ReactDOM = require('react-dom/server');
const { TemplateCompilerComponent } = require('@sqs/visitor-react-components');
const net = require('net');

const server = net.createServer((socket) => {
  console.log('client connected');
  
  socket
    .setEncoding('utf8')
    .on('data', (data) => {
      try {
        const { name, props } = JSON.parse(data.substring(2));
        const htmlOutput = ReactDOM.renderToString(
          React.createElement(TemplateCompilerComponent, { name, props }),
        );

        socket.write(`${htmlOutput}\n`);
      } catch {
        socket.write(`Invalid input given: ${data}\n`);
      }
    })
    .on('end', () => {
      console.log('client disconnected');
    });
});

server.on('error', (error) => {
  console.error(error);
});

server.listen(8124, () => {
  console.log('react renderer listening on 8124');
});
