module Page.SignTest exposing (..)

import Test exposing (..)
import Url



-- Test data


testUrl : Url.Url
testUrl =
    { protocol = Url.Http
    , host = "localhost"
    , port_ = Just 8080
    , path = "/sign/sig-123"
    , query = Just "pdf=https%3A%2F%2Fexample.com%2Ftest.pdf"
    , fragment = Nothing
    }
