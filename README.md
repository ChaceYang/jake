# jake
A distributed unique ID generator inspired by [snowflake](https://blog.twitter.com/2010/announcing-snowflake) and [sonyflake](https://github.com/sony/sonyflake)

A Jake ID is composed of
```sh
# max of 68year.
41 bits for time in units of 1 millisecond
# default is private ipv4 [0.0.11111.11111111][*.*.0-31.0-255]
13 bits for a machine id
# max of 512 sequence per millisecond
9  bits for a sequence number
```
## Usage
```sh
# gradle
# maven
```

## License

The MIT License (MIT)

See [LICENSE](https://github.com/funcfoo/jake/blob/master/LICENSE) for details.
