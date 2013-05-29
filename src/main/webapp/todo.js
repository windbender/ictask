if (typeof String.prototype.startsWith != 'function') {
	// see below for better implementation!
	String.prototype.startsWith = function(str) {
		return this.indexOf(str) == 0;
	};
}

if (typeof Array.prototype.contains != 'function') {
	Array.prototype.contains = function(obj) {
		var i = this.length;
		while (i--) {
			if (this[i] === obj) {
				return true;
			}
		}
		return false;
	};
}
if (typeof Array.prototype.remove != 'function') {
	Array.prototype.remove = function(val) {
		for ( var i = 0; i < this.length; i++) {
			if (this[i] === val) {
				this.splice(i, 1);
				i--;
			}
		}
		return this;
	};
}

var app = angular.module('todo', [ 'ngCookies', 'ngResource', 'ui', 'http-auth-interceptor' ]);

app.config(function(authServiceProvider) {
	authServiceProvider.addIgnoreUrlExpression(function(response) {
		return response.config.url === "auth/login";
	});
});

app.factory('User', function($resource) {
	var User = $resource('/ictask/users/:id',
			{}, {
				'query':  {method:'GET', isArray:true}
			});
	return User;
});

app.factory('Task', function($resource) {
	var Task = $resource('/ictask/jictask/items/:id',
			{}, {
				update : {
					method : 'PUT'
				}
			});

	Task.prototype.$remove = function() {
		Task.remove({
			id : this._id.$oid
		});
	};

	Task.prototype.$update = function() {
		return Task.update({
			id : this._id.$oid
		}, angular.extend({}, this, {
			_id : undefined
		}));
	};

	Task.prototype.done = false;
	Task.prototype.imgUpUrl = "images/up.png";
	Task.prototype.imgDownUrl = "images/down.png";
	Task.prototype.votes = [];
	Task.prototype.notes = [];

	Task.prototype.votescore = function() {
		return this.votes.reduce(function(count, vote) {
			if (vote.startsWith('-')) {
				return count - 1;
			} else {
				return count + 1;
			}
		}, 0);
	};

	
	return Task;
});

// override the default input to update on blur
app.directive('ngModelOnblur', function() {
	return {
		restrict : 'A',
		require : 'ngModel',
		link : function(scope, elm, attr, ngModelCtrl) {
			if (attr.type === 'radio' || attr.type === 'checkbox')
				return;

			elm.unbind('input').unbind('keydown').unbind('change');
			elm.bind('blur', function() {
				scope.$apply(function() {
					ngModelCtrl.$setViewValue(elm.val());
				});
			});
		}
	};
});
app.directive('autoComplete', function($timeout) {
	return function(scope, iElement, iAttrs) {
		scope.$watch(iAttrs.uiTasks, function(values) {
			iElement.autocomplete({
				source : values,
				select : function() {
					setTimeout(function() {
						iElement.trigger('input');
					}, 0);
				}
			});

		}, true);
	};
});

app.directive('authDemoApplication', function() {
	return {
		restrict : 'C',
		link : function(scope, elem, attrs) {
			// once Angular is started, remove class:
			elem.removeClass('waiting-for-angular');

			var login = elem.find('#login-holder');
			var main = elem.find('#content');

			login.hide();

			scope.$on('event:auth-loginRequired', function() {
				login.slideDown('slow', function() {
					main.hide();
				});
			});
			scope.$on('event:auth-loginConfirmed', function() {
				main.show();
				login.slideUp();
			});
		}
	};
});

app.factory('CurUser',function() {
	return {
		username:"(none)"
	};
});

app.factory('CurEditTask', function() {
	var taskBeingEdited = {};
	return {
		setEditTask: function(task) {
			taskBeingEdited = task;
		},
		getEditTask: function() {
			return taskBeingEdited;
		}
	
	};
	

});

app.controller('App', function($cookies, $scope, $rootScope, $http, $window, User, Task, CurUser, CurEditTask) {
	$scope.curUser = CurUser;
	$scope.curEditTask = CurEditTask;
	$scope.predicate = 'votescore()';
	$scope.reverse = true;
	
	
	$http({method: 'GET', url: '/ictask/users'}).
		success(function(data,status,headers,config) {
			$scope.users = [];
			for(u in data) {
				var au = data[u];
				$scope.users.push(au.username);
			}
		});
	if ($cookies['proxyAuth'] != undefined) {
		$scope.curUser.username = $cookies.proxyAuth.split(":")[0];	
	}
	$scope.tasks = Task.query();

	$scope.add = function() {
		var task = new Task({
			votes : [],
			notes : [],
			desc : $scope.newDesc,
			jobs : [ {
				"job" : "shepherd",
				"name" : ""
			} ],
			dueDate : $scope.newDueDate,
			scheduledDate : $scope.newScheduledDate,
			addedBy : $scope.curUser.username,
			committee : 'none',
			sizeInHours: 3,
			addedDate : new Date(),
			doneDate : ""
		});
		$scope.tasks.push(task);
		task.$save();
		$scope.newDesc = '';
		$scope.newDueDate = '';
		$scope.newScheduledDate = '';
		
	};

	$scope.remaining = function() {
		return $scope.tasks.reduce(function(count, task) {
			return task.done ? count : count + 1;
		}, 0);
	};

	$scope.archive = function() {
		$scope.tasks = $scope.tasks.filter(function(task) {
			if (task.done) {
				$http.post('/ictask/jictask/archived/', {
					task: task
				});
				
				task.$remove();
				return false;
			}
			return true;
		});
	};

	

	
	$scope.getClass = function(column) {
		if (column != $scope.predicate)
			return '';
		if ($scope.reverse)
			return 'sort-asc';
		return 'sort-desc';
	};

	$scope.doMarkDone = function(task) {
		if (task.done) {
			task.doneDate = new Date();
		} else {
			task.doneDate = "";
		}
		task.$update();
	};

	$scope.toggleImage = function(task, dir) {

		if (dir == 'up') {
			if (task.votes.contains($scope.curUser.username)) {
				task.votes.remove($scope.curUser.username);
			} else {
				task.votes.remove('-' + $scope.curUser.username);
				task.votes.push($scope.curUser.username);
			}
		} else {
			if (task.votes.contains('-' + $scope.curUser.username)) {
				task.votes.remove('-' + $scope.curUser.username);
			} else {
				task.votes.remove($scope.curUser.username);
				task.votes.push('-' + $scope.curUser.username);
			}
		}
		task.$update();

	};

	$scope.getVoteClass = function(task, dir) {
		if (dir == 'up') {
			if (task.votes.contains($scope.curUser.username)) {
				return "bright";
			} else if (task.votes.contains('-' + $scope.curUser.username)) {
				return "dull";
			} else {
				return "dull";
			}
		} else {
			if (task.votes.contains($scope.curUser.username)) {
				return "dull";
			} else if (task.votes.contains('-' + $scope.curUser.username)) {
				return "bright";
			} else {
				return "dull";
			}
		}
	};

	$scope.addJob = function(task, jobname) {
		task.jobs.push({
			"job" : jobname,
			"name" : ""
		});
		task.$update();
		this.newJob = "";
	};
	$scope.rmJob = function(task, job) {
		for ( var i = 0; i < task.jobs.length; i++) {
			if (task.jobs[i].job === job.job) {
				task.jobs.splice(i, 1);
				i--;
			}
		}
		task.$update();
	};
	$scope.logout = function() {
		$http.post('auth/logout').success(function() {
			$window.location.reload();
		});
	};


	$scope.addProgressNote = function(task) {
		$scope.curEditTask.setEditTask(task);
		$rootScope.$broadcast('task:progress');
		
	};
	
	$scope.editTask = function(task) {
		if (task._id == undefined) {
			$scope.curEditTask.setEditTask({});
		} else {
			$scope.curEditTask.setEditTask(task);
		}
		$rootScope.$broadcast('task:edit');
	};
	
	

	var socket = $.atmosphere;
	var subSocket;

	function subscribe() {
		var request = { url : "http://localhost:8080/ictask/pubsub/ictaskjictaskitems", transport: "long-polling"};

	    request.onMessage = function (response) {
	        if (response.status == 200) {
	            var data = response.responseBody;
	            if (data.length > 0) {
	            	var obj = jQuery.parseJSON( data );
	            	if (obj.method === 'POST') {
	            		var newTask = new Task(obj.msg);	            		
	            		newTask._id.$oid = obj.id;
	            		$scope.tasks.push(newTask);
	            	} else {
		            	function isTargetObject(compid) {
		            		return function(task) {
		            			return (task._id.$oid === compid);
		            		};
		            	}
		            	var matches = $scope.tasks.filter(isTargetObject(obj.id));
		            	if(obj.method === 'PUT') {
		            		// update!!!
			            	$rootScope.$apply(function() {
			            		matches[0].desc = obj.msg.desc;
				    			matches[0].votes = obj.msg.votes;
				    			matches[0].jobs = obj.msg.jobs;
				    			matches[0].dueDate = obj.msg.dueDate;
				    			matches[0].scheduledDate = obj.msg.scheduledDate;
				    			matches[0].addedDate = obj.msg.addedDate;
				    			matches[0].addedBy = obj.msg.addedBy;
				    			matches[0].committee = obj.msg.committee;
				    			matches[0].sizeInHours = obj.msg.sizeInHours;
				    			matches[0].doneDate = obj.msg.doneDate;
				            	});
		            	} else {
		            		// This should be DELETE
		            		matches[0].$remove();
		            	}            	
	            	}
	            }
	        }
	    };

	    subSocket = socket.subscribe(request);
	}

	subscribe();
});

app.controller({
	LoginController : function($cookies, $scope, $rootScope, $http,authService, CurUser) {
		$scope.curUser = CurUser;

		$scope.submit = function() {
			$scope.failMsg = "";
			$http.post('auth/login', {
				username : $scope.username,
				password : $scope.password
			}).success(function() {
				authService.loginConfirmed();
				var parts = $scope.username.split(":");
				$scope.curUser.username = parts[0];
			}).error(function(data, status, headers, config) {
				$scope.failMsg = "sorry that user or password invalid";
			});
		};

	}

});

function TaskEditModalController($scope, $rootScope, $http, CurUser, CurEditTask, Task) {
	$scope.curUser = CurUser;
	$scope.newTask = false;
	// zero out the fields
	
	$scope.committees = [
	                     {"ID":"Maintenance","Title":"Maintenance"},
	                     {"ID":"Land Plans","Title":"Land Plans"},
	                     {"ID":"Tiller Rillers","Title":"Tiller Rillers"},
	                     {"ID":"Finance","Title":"Finance"},
	                     {"ID":"W.E.T.","Title":"W.E.T"},
	                     {"ID":"Chicken Group","Title":"Chicken Group"},
	                     {"ID":"none","Title":"none"},
	                     
	           ];

	// if we're starting the edit what do we do ?
	$rootScope.$on('task:edit', function() {
		var editTask = CurEditTask.getEditTask();
		if (editTask._id) {
			$scope.newTask = false;
			// copy the "old" data into the current scope for use
			$scope._id = editTask._id;
			$scope.desc = editTask.desc;
			$scope.votes = editTask.votes;
			$scope.jobs = editTask.jobs;
			$scope.dueDate = editTask.dueDate;
			$scope.scheduledDate = editTask.scheduledDate;
			$scope.addedDate = editTask.addedDate;
			$scope.addedBy = editTask.addedBy;
			$scope.committee = editTask.committee;
			$scope.sizeInHours = editTask.sizeInHours;
			$scope.doneDate = editTask.doneDate;
		} else {
			// zero out the data since this is a new task
			$scope.newUser = true;
			$scope.incomplete = true;
			$scope._id = editTask._id;
			$scope.desc = 'pound a nail';
			$scope.votes = [];
			$scope.notes = [];
			$scope.jobs = [ {
				"job" : "shepherd",
				"name" : ""
			} ];
			$scope.dueDate = '';
			$scope.scheduledDate = '';
			$scope.addedDate = new Date();
			$scope.addedBy = $scope.curUser.username;
			$scope.committee = 'none';
			$scope.sizeInHours = 3;
			$scope.doneDate = '';
		}
	});
	
	$rootScope.$on('task:submit', function() {
		// ok copy the edited data back
		var editTask = CurEditTask.getEditTask();

		editTask.desc = $scope.desc;
		editTask.dueDate = $scope.dueDate;
		editTask.scheduledDate = $scope.scheduledDate;
		editTask.committee = $scope.committee;
		editTask.sizeInHours = $scope.sizeInHours;
		editTask.$update();
		
		$scope.newTask = false;
		// copy the "old" data into the current scope for use
		$scope.desc = "";
		$scope.votes = "";
		$scope.jobs = "";
		$scope.dueDate = "";
		$scope.scheduledDate = "";
		$scope.addedDate = "";
		$scope.addedBy = "";
		$scope.committee = "";
		$scope.sizeInHours = "";
		$scope.doneDate = "";
	});
	
	$rootScope.$on('task:cancel', function() {
		/// um don't move the data
	});
		
	$scope.presubmit = function () {
		$rootScope.$broadcast('task:submit');
	};
	$scope.precancel = function () {
		$rootScope.$broadcast('task:cancel');
	};
	
	$scope.$watch('sizeInHours', function() {
        $scope.sizeInHours = parseFloat($scope.sizeInHours);
    });
}

function AddProgressModalController($scope, $rootScope, $http, CurUser, CurEditTask, Task) {
	$scope.task = Task;
	$scope.curUser = CurUser;
	$scope.newTask = false;
	// zero out the fields
	$scope.incomplete = true;
	// if we're starting the edit what do we do ?
	$rootScope.$on('task:progress', function() {
		// since this is sort of an add only thing...do we need to add something ?
	});
	
	$rootScope.$on('task:submitprogress', function() {
		// ok copy the edited data back
		var editTask = CurEditTask.getEditTask();
		if(!editTask.hasOwnProperty('notes')) {
			editTask.notes = [];
		}
		var aNote = {
			"who" : CurUser.username,
			"note" : $scope.progressNote
		};
		editTask.notes.push(aNote);
		editTask.$update();
	});
	
	$rootScope.$on('task:cancelprogress', function() {
		/// um don't move the data
	});
	
	$scope.$watch('progressNote', function() {
	    $scope.incompleteTest();
	});
	$scope.incompleteTest = function() {
		if( $scope.progressNote.length > 1) {
			$scope.incomplete= false;
		} else {
			$scope.incomplete=true;
		}
	  };
	
	
	$scope.presubmit = function () {
		$rootScope.$broadcast('task:submitprogress');
	};
	$scope.precancel = function () {
		$rootScope.$broadcast('task:cancelprogress');
	}
;
}


