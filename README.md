Hanabi Bot
==========

[![Build Status](https://travis-ci.org/jingw/hanabi-bot.svg?branch=master)](https://travis-ci.org/jingw/hanabi-bot)

This project is a framework for programatically playing Hanabi.

 - Run tests and generate coverage report: `sbt jacoco:cover`
 - Run main class: `sbt run`

It currently contains two AIs:

- a `SmartPlayer` that uses an unconventional strategy to cram as much information as possible into each hint
- a `HumanStylePlayer` that emulates and builds on human-style Hanabi strategies
