import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let host
let port
let timestamp = new Date()

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> <host> <port>')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    host = args.host
    port = args.port
    // net.connect(port[,host][connectListener]) -> returns new 'net.Socket'; default localhost
    server = connect({ host: host, port: port }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => {
      this.log(Message.fromJSON(buffer).toString())
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input)
    const contents = rest.join(' ')

    if (command === 'disconnect') {
      server.end(new Message({ timestamp, username, command }).toJSON() + '\n')
    } else if (command === 'echo') {
      server.write(new Message({ timestamp, username, command, contents }).toJSON() + '\n')
    } else if (command === 'broadcast') {
      server.write(new Message({ timestamp, username, command, contents }).toJSON() + '\n')
    } else if (command === '@<username>') {
      server.write(new Message({ timestamp, username, command, contents }).toJSON() + '\n')
    } else if (command === 'users') {
      server.write(new Message({ timestamp, username, command, contents }).toJSON() + '\n')
    } else {
      this.log(`Command <${command}> was not recognized.  Please enter usable Command`)
    }

    callback()
  })
