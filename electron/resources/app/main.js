require('uppercase-core');

const {app, BrowserWindow, shell} = require('electron');
const ipcMain = require('electron').ipcMain;

const Path = require('path');
const URL = require('url');

app.commandLine.appendSwitch('in-process-gpu');

let win;
let createWindow = () => {
	
	let winConfig = {};
	
	win = new BrowserWindow({
		icon : __dirname + '/favicon.ico',
		width : 1280,
		height : 720,
		useContentSize : true
	});
	
	ipcMain.on('toggleDevTool', () => {
		if (win.webContents.isDevToolsOpened() !== true) {
			win.webContents.openDevTools();
		} else {
			win.webContents.closeDevTools();
		}
	});
	
	ipcMain.on('winConfig', (e, _winConfig) => {
		
		if (_winConfig !== undefined && _winConfig !== null) {
			winConfig = _winConfig;
			
			if (winConfig.isMaximized === true) {
				win.maximize();
			}
			
			else {
				
				if (winConfig.width !== undefined && winConfig.height !== undefined) {
					win.setSize(winConfig.width, winConfig.height);
				}
				
				if (winConfig.x !== undefined && winConfig.y !== undefined) {
					win.setPosition(winConfig.x, winConfig.y);
				}
			}
		}
		
		else {
			win.maximize();
		}
	});

	win.loadURL(URL.format({
		pathname : Path.join(__dirname, 'index.html'),
		protocol : 'file:',
		slashes : true
	}));
	
	win.setMenu(null);
	
	let setConfig = () => {
		
		let bounds = win.getBounds();
		
		winConfig.isMaximized = win.isMaximized();
		
		if (winConfig.isMaximized !== true) {
			winConfig.x = bounds.x;
			winConfig.y = bounds.y;
			winConfig.width = bounds.width;
			winConfig.height = bounds.height;
		}
	};
	
	win.on('move', () => {
		setConfig();
	});

	win.on('close', () => {
		
		setConfig();
		
		win.webContents.send('winConfig', winConfig);
	});

	win.on('closed', () => {
		win = undefined;
	});
	
	win.webContents.on('new-window', (event, url) => {
		event.preventDefault();
		shell.openExternal(url);
	});
};

app.on('ready', createWindow);

app.on('window-all-closed', () => {
	if (process.platform !== 'darwin') {
		app.quit();
	}
});

app.on('activate', () => {
	if (win === null) {
		createWindow();
	}
});