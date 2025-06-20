module Route exposing (Route(..), fromUrl)

import Url exposing (Url)
import Url.Parser exposing (..)


type Route
    = RouteHome
    | RouteWaiver String
    | RouteSign String
    | RouteAdmin
    | RouteAdminProjects
    | RouteAdminProjectDetail String
    | RouteAdminSignatures
    | RouteSignatureStatus String


fromUrl : Url -> Maybe Route
fromUrl url =
    parse matchRoute url


matchRoute : Parser (Route -> a) a
matchRoute =
    oneOf
        [ map RouteHome top
        , map RouteWaiver (s "waiver" </> string)
        , map RouteSign (s "sign" </> string)
        , map RouteAdmin (s "admin")
        , map RouteAdminProjects (s "admin" </> s "projects")
        , map RouteAdminProjectDetail (s "admin" </> s "projects" </> string)
        , map RouteAdminSignatures (s "admin" </> s "signatures")
        , map RouteSignatureStatus (s "signatures" </> string)
        ]