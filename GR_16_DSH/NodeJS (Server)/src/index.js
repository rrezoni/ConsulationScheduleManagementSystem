'use strict'

const dgram = require('dgram');
const ws = require('ws');
const net = require('net');
const mysql = require('mysql');

const udpServerSocket = dgram.createSocket('udp4');
const wsServerSocket = new ws.Server({ port: 13000, host: 'localhost' }, console.log("WebSocket Server is active."));
const tcpServerSocket = net.createServer({ allowHalfOpen: true });
const mysqlConnection = mysql.createConnection({ host: "localhost", database: "dsh_database", user: "root", password: "123123", dateStrings: true });

const webSockets = {};
const udpRemoteInfos = {};
const tcpSockets = {};

setInterval(() => {
    var ids = [];
    for (var id in webSockets)
        ids.push("'" + id + "'");
    if (ids.length != 0) {
        new Promise((resolve) => {
            mysqlConnection.query("select pusername from professor where pid in (" + ids + ");", (err, results) => {
                var usernames = [];
                if (err)
                    console.log("Error");
                else {
                    results.forEach(element => {
                        usernames.push("'" + element.pusername + "'");
                    });
                    resolve(usernames);
                }
            })
        }).then((usernames) => {
            return new Promise((resolve) => mysqlConnection.query("select susername from student where sid in (" + ids + ");", (err, results) => {
                if (err)
                    console.log(err);
                else {
                    results.forEach(element => {
                        usernames.push("'" + element.susername + "'");
                    });
                    resolve(usernames);
                }
            }));
        }).then((usernames) => {
            for (var id in webSockets) {
                try {
                    webSockets[id].send('{"type":"OnlineUsers","onlineUsers":[' + usernames + ']}');
                } catch (e) {
                    console.log(e);
                }
            }
        });
    }
}, 5000);

udpServerSocket.bind(13000, 'localhost', () => {
    console.log('UDP Server is active.');

    udpServerSocket.setSendBufferSize(65536);
    udpServerSocket.setRecvBufferSize(65536);
});

udpServerSocket.on('message', (message, remoteInfo) => {
    try {
        var messageAsJSON = JSON.parse(message.toString());

        if (messageAsJSON.type == "Connect") {
            var sourceID = messageAsJSON.sourceID;
            udpRemoteInfos[sourceID] = remoteInfo;
        } else if (messageAsJSON.type == "VideoFrame") {
            var targetID = messageAsJSON.targetID;
            udpServerSocket.send(message.toString(), udpRemoteInfos[targetID].port, udpRemoteInfos[targetID].address);
        } else if (messageAsJSON.type == "AudioData") {
            var targetID = messageAsJSON.targetID;
            udpServerSocket.send(message.toString(), udpRemoteInfos[targetID].port, udpRemoteInfos[targetID].address);
        }

    } catch (e) {
        console.log(e);
    }
});

udpServerSocket.on('error', (err) => {
    console.log(err);
});

wsServerSocket.on('connection', (ws) => {

    var sourceID;

    ws.on('message', (data) => {

        try {
            var dataAsJson = JSON.parse(data.toString());

            if (dataAsJson.type == "Connect") {
                sourceID = dataAsJson.sourceID;
                webSockets[sourceID] = ws;
            } else if (dataAsJson.type == "TextData") {
                webSockets[dataAsJson.targetID].send(data.toString());
            } // else if (dataAsJson.type == "VoiceCallRequest") {
            //     try {
            //         console.log("Voice call");
            //         webSockets[dataAsJson.targetID].send(data.toString());
            //     } catch (e) {
            //         console.log(e);
            //     }
            // } else if (dataAsJson.type == "VideoCallRequest") {
            //     try {
            //         console.log("Video call");
            //         webSockets[dataAsJson.targetID].send(data.toString());
            //     } catch (e) {
            //         console.log(e);
            //     }
            // }
        } catch (e) { }
    });

    ws.on('close', (code, reason) => {
        console.log("WebSocket Code " + code + ": " + reason);

        try {
            ws.close();
            delete webSockets[sourceID];
            tcpSockets[sourceID].destroy();
            delete tcpSockets[sourceID];
            delete udpRemoteInfos[sourceID];
        } catch (e) { }
    });

    ws.on('error', (err) => {
        console.log(err);

        try {
            ws.close();
            delete webSockets[sourceID];
            tcpSockets[sourceID].destroy();
            delete tcpSockets[sourceID];
            delete udpRemoteInfos[sourceID];
        } catch (e) { }
    });

});

tcpServerSocket.on('connection', (socket) => {

    var sourceID;

    socket.on('data', (data) => {
        try {
            console.log(data.toString());
            var dataAsJson = JSON.parse(data.toString());
            if (dataAsJson.type == "LoginRequest") {
                mysqlConnection.query("select * from student where (sid='" + dataAsJson.username + "' or susername = '" + dataAsJson.username + "') and spassword = '" + dataAsJson.password + "';", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "LoginResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                    else {
                        if (results.length == 1) {
                            var response = {
                                type: "LoginResponse",
                                description: "Successful",
                                user: "Student",
                                info: results[0]
                            }
                            socket.write(JSON.stringify(response) + "\r\n");
                            tcpSockets[dataAsJson.sourceID] = socket;
                        } else {
                            mysqlConnection.query("select * from professor where (pid = '" + dataAsJson.username + "' or pusername = '" + dataAsJson.username + "') and ppassword = '" + dataAsJson.password + "';", (err, results) => {
                                if (err) {
                                    console.log(err);
                                    var response = {
                                        type: "LoginResponse",
                                        description: "Failed"
                                    }
                                    socket.write(JSON.stringify(response) + "\r\n");
                                }
                                else {
                                    if (results.length == 1) {
                                        var response = {
                                            type: "LoginResponse",
                                            description: "Successful",
                                            user: "Professor",
                                            info: results[0]
                                        }
                                        socket.write(JSON.stringify(response) + "\r\n");
                                        tcpSockets[dataAsJson.sourceID] = socket;
                                    } else {
                                        mysqlConnection.query("select * from admin where ausername = '" + dataAsJson.username + "' and apassword = '" + dataAsJson.password + "';", (err, results) => {
                                            if (err) {
                                                console.log(err);
                                                var response = {
                                                    type: "LoginResponse",
                                                    description: "Failed"
                                                }
                                                socket.write(JSON.stringify(response) + "\r\n");
                                            } else {
                                                if (results.length == 1) {
                                                    var response = {
                                                        type: "LoginResponse",
                                                        description: "Successful",
                                                        user: "Admin",
                                                        info: results[0]
                                                    }
                                                    socket.write(JSON.stringify(response) + "\r\n");
                                                    tcpSockets[dataAsJson.sourceID] = socket;
                                                } else {
                                                    var response = {
                                                        type: "LoginResponse",
                                                        description: "Failed"
                                                    }
                                                    socket.write(JSON.stringify(response) + "\r\n");
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
            } else if (dataAsJson.type == "SignupRequest") {
                mysqlConnection.query("insert into account_request (rfirstname, rlastname, rbirthdate, rgender, rusername, rsid, rpassword, ryear, rdepartment) values ('" + dataAsJson.firstname + "', '" + dataAsJson.lastname + "', '" + dataAsJson.birthdate + "', '" + dataAsJson.gender + "', '" + dataAsJson.username + "', '" + dataAsJson.id + "', '" + dataAsJson.password + "', '" + dataAsJson.studyyear + "', '" + dataAsJson.department + "');", (err) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "SignupResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "SignupResponse",
                            description: "Successful"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "DeleteConsultation") {
                mysqlConnection.query("delete from consultation where cid = " + dataAsJson.cid + " and (pid, subid) = ('" + dataAsJson.pid + "', " + dataAsJson.subid + ");", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "DeleteConsultationResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "DeleteConsultationResponse",
                            description: "Successful"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "ConsultationsTableRequest") {
                mysqlConnection.query("select * from consultation, professor, subject where (professor.pid, subject.subid) = (consultation.pid, consultation.subid);", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "ConsultationsTableResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "ConsultationsTableResponse",
                            description: "Successful",
                            consultations: results
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "UserInfoRequest") {
                mysqlConnection.query("select * from student where susername = '" + dataAsJson.username + "';", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "UserInfoResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        if (results.length == 1) {
                            var response = {
                                type: "UserInfoResponse",
                                description: "Successful",
                                user: "Student",
                                userinfo: results[0]
                            }
                            socket.write(JSON.stringify(response) + "\r\n");
                        } else {
                            mysqlConnection.query("select * from professor where pusername = '" + dataAsJson.username + "';", (err, results) => {
                                if (err) {
                                    console.log(err);
                                    var response = {
                                        type: "UserInfoResponse",
                                        description: "Failed"
                                    }
                                    socket.write(JSON.stringify(response) + "\r\n");
                                } else {
                                    if (results.length == 1) {
                                        var response = {
                                            type: "UserInfoResponse",
                                            description: "Successful",
                                            user: "Professor",
                                            userinfo: results[0]
                                        }
                                        socket.write(JSON.stringify(response) + "\r\n");
                                    } else {
                                        var response = {
                                            type: "UserInfoResponse",
                                            description: "NoUserInfo",
                                        }
                                        socket.write(JSON.stringify(response) + "\r\n");
                                    }
                                }
                            });
                        }
                    }
                });
            } else if (dataAsJson.type == "ProfessorSubjectNamesRequest") {
                mysqlConnection.query("select subname from subject, professor_subject where subject.subid = professor_subject.subid and professor_subject.pid = '" + dataAsJson.pid + "';", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "ProfessorSubjectNamesResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "ProfessorSubjectNamesResponse",
                            description: "Successful",
                            subjects: results
                        }
                        console.log(JSON.stringify(response));
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "ChangeUsernameRequest") {
                mysqlConnection.query("select " + ((dataAsJson.user == "Student") ? "spassword from student where sid" : "ppassword from professor where pid") + " = '" + dataAsJson.id + "';", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "ChangeUsernameResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        if (results.length == 1) {
                            if (results[0].spassword == dataAsJson.givenpassword || results[0].ppassword == dataAsJson.givenpassword) {
                                mysqlConnection.query("update " + ((dataAsJson.user == "Student") ? ("student set susername = '" + dataAsJson.newusername + "' where sid = '" + dataAsJson.id + "'") : ("professor set pusername = '" + dataAsJson.newusername + "' where pid = '" + dataAsJson.id + "'")) + ";", (err, results) => {
                                    if (err) {
                                        console.log(err);
                                        var response = {
                                            type: "ChangeUsernameResponse",
                                            description: "Failed"
                                        }
                                        socket.write(JSON.stringify(response) + "\r\n");
                                    } else {
                                        var response = {
                                            type: "ChangeUsernameResponse",
                                            description: "Successful"
                                        }
                                        socket.write(JSON.stringify(response) + "\r\n");
                                    }
                                });
                            } else {
                                console.log(err);
                                var response = {
                                    type: "ChangeUsernameResponse",
                                    description: "WrongPassword"
                                }
                                socket.write(JSON.stringify(response) + "\r\n");
                            }
                        } else {
                            console.log(err);
                            var response = {
                                type: "ChangeUsernameResponse",
                                description: "WrongPassword"
                            }
                            socket.write(JSON.stringify(response) + "\r\n");
                        }
                    }
                });
            } else if (dataAsJson.type == "ChangePasswordRequest") {
                mysqlConnection.query("select " + ((dataAsJson.user == "Student") ? "spassword from student where sid" : "ppassword from professor where pid") + " = '" + dataAsJson.id + "';", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "ChangePasswordResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        if (results.length == 1) {
                            if (results[0].spassword == dataAsJson.givenpassword || results[0].ppassword == dataAsJson.givenpassword) {
                                mysqlConnection.query("update " + ((dataAsJson.user == "Student") ? ("student set spassword = '" + dataAsJson.newpassword + "' where sid = '" + dataAsJson.id + "'") : ("professor set ppassword = '" + dataAsJson.newpassword + "' where pid = '" + dataAsJson.id + "'")) + ";", (err, results) => {
                                    if (err) {
                                        console.log(err);
                                        var response = {
                                            type: "ChangePasswordResponse",
                                            description: "Failed"
                                        }
                                        socket.write(JSON.stringify(response) + "\r\n");
                                    } else {
                                        var response = {
                                            type: "ChangePasswordResponse",
                                            description: "Successful"
                                        }
                                        socket.write(JSON.stringify(response) + "\r\n");
                                    }
                                });
                            } else {
                                var response = {
                                    type: "ChangeUsernameResponse",
                                    description: "WrongPassword"
                                }
                                socket.write(JSON.stringify(response) + "\r\n");
                            }
                        } else {
                            var response = {
                                type: "ChangePasswordResponse",
                                description: "WrongPassword"
                            }
                            socket.write(JSON.stringify(response) + "\r\n");
                        }
                    }
                });
            } else if (dataAsJson.type == "DeclineAccountRequest") {
                mysqlConnection.query("delete from account_request where rid = " + dataAsJson.id + ";", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "DeclineAccountResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "DeclineAccountResponse",
                            description: "Successful"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "AcceptAccountRequest") {
                mysqlConnection.query("insert into student (sid, sfirstname, slastname, susername, spassword, sgender, sbirthdate, sdepartment, syear) select rsid, rfirstname, rlastname, rusername, rpassword, rgender, rbirthdate, rdepartment, ryear from account_request where rid = '" + dataAsJson.rid + "';", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "AcceptAccountResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "AcceptAccountResponse",
                            description: "Successful"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "AccountRequestTableRequest") {
                mysqlConnection.query("select * from account_request;", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "AccountRequestTableResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "AccountRequestTableResponse",
                            description: "Successful",
                            accountrequests: results
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "DeleteStudentRequest") {
                mysqlConnection.query("delete from student where sid = '" + dataAsJson.sid + "';", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "DeleteStudentResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "DeleteStudentResponse",
                            description: "Successful"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "StudentTableRequest") {
                mysqlConnection.query("select * from student;", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "StudentTableResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "StudentTableResponse",
                            description: "Successful",
                            students: results
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "AddProfessorRequest") {
                mysqlConnection.query("insert into professor (pid, pfirstname, plastname, pusername, ppassword, pbirthdate, pgender) values ('" + dataAsJson.id + "', '" + dataAsJson.firstname + "', '" + dataAsJson.lastname + "', '" + dataAsJson.username + "', '" + dataAsJson.password + "', '" + dataAsJson.birthdate + "', '" + dataAsJson.gender + "');", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "AddProfessorResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "AddProfessorResponse",
                            description: "Successful"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "DeleteProfessorRequest") {
                mysqlConnection.query("delete from professor where pid = '" + dataAsJson.pid + "';", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "DeleteProfessorResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "DeleteProfessorResponse",
                            description: "Successful"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "ProfessorTableRequest") {
                mysqlConnection.query("select * from professor;", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "ProfessorTableResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "ProfessorTableResponse",
                            description: "Successful",
                            professors: results
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "AddSubjectRequest") {
                mysqlConnection.query("insert into subject (subname, subyear) values ('" + dataAsJson.subname + "', '" + dataAsJson.subyear + "');", (err, results) => {
                    console.log(results);
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "AddSubjectResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        mysqlConnection.query("select * from subject where subname = '" + dataAsJson.subname + "';", (err, results) => {
                            if (err) {
                                console.log(err);
                                var response = {
                                    type: "AddSubjectResponse",
                                    description: "Failed"
                                }
                                socket.write(JSON.stringify(response) + "\r\n");
                            } else {
                                var response = {
                                    type: "AddSubjectResponse",
                                    description: "Successful",
                                    subject: results[0]
                                }
                                socket.write(JSON.stringify(response) + "\r\n");
                            }
                        });
                    }
                });
            } else if (dataAsJson.type == "DeleteSubjectRequest") {
                mysqlConnection.query("delete from subject where subid = '" + dataAsJson.subid + "';", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "DeleteSubjectResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "DeleteSubjectResponse",
                            description: "Successful"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "SubjectTableRequest") {
                mysqlConnection.query("select * from subject;", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "SubjectTableResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "SubjectTableResponse",
                            description: "Successful",
                            subjects: results
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "AddProfessorSubjectRequest") {
                mysqlConnection.query("insert into professor_subject (subid, pid) values (" + dataAsJson.subid + ", '" + dataAsJson.pid + "');", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "AddProfessorSubjectResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "AddProfessorSubjectResponse",
                            description: "Successful"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "DeleteProfessorSubjectRequest") {
                mysqlConnection.query("delete from professor_subject where subid = '" + dataAsJson.subid + "' and pid = '" + dataAsJson.pid + "';", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "DeleteProfessorSubjectResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "DeleteProfessorSubjectResponse",
                            description: "Successful"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "ProfessorSubjectTableRequest") {
                mysqlConnection.query("select * from professor_subject;", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "ProfessorSubjectTableResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "ProfessorSubjectTableResponse",
                            description: "Successful",
                            professorsubjects: results
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "DeleteConsultationRequest") {
                mysqlConnection.query("delete from consultation where cid = '" + dataAsJson.cid + "';", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "DeleteConsultationResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "DeleteConsultationResponse",
                            description: "Successful"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "ConsultationTableRequest") {
                mysqlConnection.query("select * from consultation;", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "ConsultationTableResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var response = {
                            type: "ConsultationTableResponse",
                            description: "Successful",
                            consultations: results
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    }
                });
            } else if (dataAsJson.type == "AddConsultationRequest") {
                mysqlConnection.query("select * from consultation, subject where subject.subyear = (select subyear from subject where subname = '" + dataAsJson.subname + "');", (err, results) => {
                    if (err) {
                        console.log(err);
                        var response = {
                            type: "AddConsultationResponse",
                            description: "Failed"
                        }
                        socket.write(JSON.stringify(response) + "\r\n");
                    } else {
                        var c = false;
                        for (var i = 0; i < results.length; i++) {
                            if ((results[i].cstart > new Date(dataAsJson.cstart) && results[i].cstart < new Date(dataAsJson.cend)) || (results[i].cend > new Date(dataAsJson.cstart)) && (results[i].cend < new Date(dataAsJson.cend)) || (new Date(dataAsJson.cstart) < new Date(Date.now())) || (new Date(dataAsJson.cend) < new Date(Date.now()))) {
                                c = true;
                                console.log(c);
                                break;
                            }
                        }
                        if (c) {
                            var response = {
                                type: "AddConsultationResponse",
                                description: "ConsultationError"
                            }
                            socket.write(JSON.stringify(response) + "\r\n");
                        } else {
                            mysqlConnection.query("insert into consultation (pid, subid, cstart, cend, cdescription) values ('" + dataAsJson.pid + "', (select subid from subject where subname = '" + dataAsJson.subname + "'), '" + dataAsJson.cstart + "', '" + dataAsJson.cend + "', '" + dataAsJson.description + "');", (err, results) => {
                                if (err) {
                                    console.log(err);
                                    var response = {
                                        type: "AddConsultationResponse",
                                        description: "ConsultationError"
                                    }
                                    socket.write(JSON.stringify(response) + "\r\n");
                                } else {
                                    var response = {
                                        type: "AddConsultationResponse",
                                        description: "Successful"
                                    }
                                    socket.write(JSON.stringify(response) + "\r\n");
                                }
                            });
                        }
                    }
                });
            }
        } catch (e) {
            console.log(e);
            var response = {
                type: "LoginResponse",
                description: "Failed"
            }
            socket.write(JSON.stringify(response) + "\r\n");
        }
    });

    socket.on('close', (had_error) => {
        console.log(`TCP Close`);

        try {
            socket.destroy();
            delete tcpSockets[sourceID];
            ws.close();
            delete webSockets[sourceID];
            delete udpRemoteInfos[sourceID];
        } catch (e) { }
    });

    socket.on('error', (err) => {
        try {
            socket.destroy();
            delete tcpSockets[sourceID];
            ws.close();
            delete webSockets[sourceID];
            delete udpRemoteInfos[sourceID];
        } catch (e) { }
    });

});

tcpServerSocket.listen(13001, "localhost", () => {
    console.log("TCP server is active.");
});

mysqlConnection.connect((err) => {
    if (err)
        console.log(err);
    console.log("Connected to MySQL server.");
});