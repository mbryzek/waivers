module Constants exposing (anonParams, apiHost)

import Generated.IoBryzekWaiversApi exposing (HttpRequestParams)


apiHost : String
apiHost =
    "http://localhost:9300"


anonParams : HttpRequestParams
anonParams =
    { apiHost = apiHost
    , headers = []
    }
