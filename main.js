const express = require('express');
const ewelink = require('ewelink-api');
var bodyParser = require('body-parser');

const app = express();
app.use(bodyParser.urlencoded({ extended: true }));

app.get('/', async (req, res) => {  
  res.send('eWeLink API Server');
});

app.post('/on/:deviceid', async (req, res) => {  
  console.log('on',req.params.deviceid);
  const connection = new ewelink({
    email: req.body.email,
    password: req.body.password,
    region: req.body.region,
  });
  const result = await setDevicePowerState(connection, req.params.deviceid,'on');
  res.type('json');
  res.send(result);
});

app.post('/off/:deviceid', async (req, res) => {
  console.log('off',req.params.deviceid);
  const connection = new ewelink({
    email: req.body.email,
    password: req.body.password,
    region: req.body.region,
  });
  const result = await setDevicePowerState(connection, req.params.deviceid,'off');
  res.type('json');
  res.send(result);
});

app.post('/toggle/:deviceid', async (req, res) => {
  console.log('toggle',req.params.deviceid);
  const connection = new ewelink({
    email: req.body.email,
    password: req.body.password,
    region: req.body.region,
  });
  const result = await setDevicePowerState(connection, req.params.deviceid,'toggle');
  res.type('json');
  res.send(result);
});

app.post('/state/:deviceid', async (req, res) => {
  console.log('state',req.params.deviceid);
  const connection = new ewelink({
    email: req.body.email,
    password: req.body.password,
    region: req.body.region,
  });
  const result = await getDevice(connection,req.params.deviceid);
  res.type('json');
  res.send(result);
});

app.get('/region', async (req, res) => {
  console.log('region',req.params.deviceid);
  const connection = new ewelink({
    email: req.query.email,
    password: req.query.password
  });
  const result = await connection.getRegion();
  res.type('json');
  res.send(result);
});


var server = app.listen(8700, function () {
  var host = server.address().address;
  var port = server.address().port;
  
  console.log('Server is working : PORT - ',port);
});

async function setDevicePowerState(connection, deviceid, cmd) {
  try {
    const status = await connection.setDevicePowerState(deviceid, cmd);
    status['result'] = "SUCCESS"; 
    console.log(status);
    return status;
  }
  catch (rejectedValue) {
    var data = new Object();
    data.result = "FAIL";
    return JSON.stringify(data);;
  }
}

async function getDevice(connection, deviceid) {
  try {
    const device = await connection.getDevice(deviceid);
    var data = new Object();
    data.result = "SUCCESS";
    if(device.online == true){
      data.status = "ok"
    }else{
      data.status = "nok"
    }
    data.state = device.params.switch
    console.log(device);  
    console.log(data);  
    return JSON.stringify(data);
  }
  catch (rejectedValue) {
    var data = new Object();
    data.result = "FAIL";
    return JSON.stringify(data);;
  }
}